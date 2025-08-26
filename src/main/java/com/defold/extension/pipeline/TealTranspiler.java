package com.defold.extension.pipeline;

import com.dynamo.bob.Platform;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TealTranspiler implements ILuaTranspiler {

    // Match severity and resource path from strings like:
    //       Warn 2 warnings in main/foo bar.tl
    //      Error 2 type errors in main/baz.tl
    public static final Pattern SEVERITY_AND_PATH = Pattern.compile("^\\s*\\w*\\s*\\d+.+(warning|error)s? in (.+)$");
    // Find line number in strings like:
    //        ...    1 │ local not_an_int: int = "asd"
    //        ...    9 │ local py = "1";
    public static final Pattern LINE = Pattern.compile("^\\s*\\.{3}\\s*(\\d+)\\s*│");
    // Match line that highlights the issue in a line above like:
    //        ...      │       ^^
    //        ...      │       ^^^^^
    public static final Pattern ERROR_UNDERLINE = Pattern.compile("^\\s*\\.{3}\\s*│\\s*\\^+\\s*$");
    // Match issue message like:
    //        ...      │ unknown type int
    //        ...      │ in local declaration: foo: expected an array: at index 2: got integer, expected string
    public static final Pattern MESSAGE = Pattern.compile("^\\s*\\.{3}\\s*│\\s*(.+)$");

    @Override
    public String getBuildFileResourcePath() {
        return "/tlconfig.lua";
    }

    @Override
    public String getSourceExt() {
        return "tl";
    }

    @Override
    public List<Issue> transpile(File pluginDir, File sourceDir, File outputDir) throws Exception {
        cleanOutput(sourceDir, outputDir);
        Platform platform = Platform.getHostPlatform();
        String exeSuffix = "win32".equals(platform.getOs()) ? ".bat" : "";
        String cmd = new File(pluginDir, "teal/plugins/bin/" + platform.getPair() + "/bin/cyan" + exeSuffix).toString();
        Process process = new ProcessBuilder(
                cmd, "build", "--build-dir", outputDir.toString(), "--prune", "--include-dir", sourceDir.toString())
                .directory(sourceDir)
                .redirectErrorStream(true)
                .start();
        List<Issue> result = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line = r.readLine();
            Severity severity = null;
            int issueLine = 0;
            String resourcePath = null;
            while (line != null) {
                lines.add(line);
                Matcher severityAndPathMatcher = SEVERITY_AND_PATH.matcher(line);
                if (severityAndPathMatcher.matches()) {
                    severity = severityAndPathMatcher.group(1).equals("warning") ? Severity.WARNING : Severity.ERROR;
                    resourcePath = "/" + severityAndPathMatcher.group(2);
                } else {
                    Matcher lineMatcher = LINE.matcher(line);
                    if (lineMatcher.find()) {
                        issueLine = Integer.parseInt(lineMatcher.group(1));
                    } else {
                        if (!ERROR_UNDERLINE.matcher(line).matches()) {
                            Matcher messageMatcher = MESSAGE.matcher(line);
                            if (messageMatcher.matches()) {
                                result.add(new Issue(
                                        severity,
                                        resourcePath,
                                        issueLine,
                                        messageMatcher.group(1)
                                ));
                                issueLine = 0;
                            }
                        }
                    }
                }
                line = r.readLine();
            }
        }

        int exitCode = process.waitFor();
        boolean success = exitCode == 0;
        if (!success && result.stream().noneMatch(x -> x.severity == Severity.ERROR)) {
            result.add(new Issue(
                    Severity.ERROR,
                    getBuildFileResourcePath(),
                    1,
                    String.join("\n", lines)));
        }
        return result;
    }

    private static void cleanOutput(File sourceDir, File outputDir) throws IOException {
        if (!outputDir.isDirectory()) {
            return;
        }

        // Cyan will only re-transpile a source file if it is newer than the
        // output file. This is an issue for us due to how the editor prepares
        // source files for compilation. As a workaround, we will clean the
        // output directory before invoking Cyan. However, Cyan can be
        // configured to selectively prune files from the output directory, so
        // even though we supply the --prune flag, simply emptying the output
        // directory is too intrusive. Instead, we specifically remove only the
        // expected output files from the output directory.
        // See https://github.com/defold/defold/issues/8928 for details.
        Path sourceDirPath = sourceDir.toPath().toRealPath(LinkOption.NOFOLLOW_LINKS);
        Path outputDirPath = outputDir.toPath().toRealPath(LinkOption.NOFOLLOW_LINKS);
        List<Path> expectedOutputFilePaths = getExpectedOutputFilePaths(sourceDirPath, outputDirPath);

        for (Path path : expectedOutputFilePaths) {
            if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                Files.delete(path);
            }
        }
    }

    private static List<Path> getExpectedOutputFilePaths(Path sourceDirPath, Path outputDirPath) throws IOException {
        if (sourceDirPath.equals(outputDirPath)) {
            // Cyan does not write any .lua files in this case.
            return List.of();
        }

        List<Path> tealSourceFilePaths = getTealSourceFilePaths(sourceDirPath);
        return getLuaOutputFilePaths(tealSourceFilePaths, sourceDirPath, outputDirPath);
    }

    private static List<Path> getTealSourceFilePaths(Path sourceDirPath) throws IOException {
        PathMatcher tealSourceFilePathMatcher = FileSystems.getDefault().getPathMatcher("glob:**.tl");
        Path buildDirPath = sourceDirPath.resolve("build");
        ArrayList<Path> tealSourceFilePaths = new ArrayList<>(128);

        Files.walkFileTree(sourceDirPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dirPath, BasicFileAttributes attributes) throws IOException {
                boolean skip = dirPath.equals(buildDirPath) || Files.isHidden(dirPath);
                return skip ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) throws IOException {
                if (tealSourceFilePathMatcher.matches(filePath) && Files.isRegularFile(filePath, LinkOption.NOFOLLOW_LINKS) && !Files.isHidden(filePath)) {
                    tealSourceFilePaths.add(filePath);
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return tealSourceFilePaths;
    }

    private static List<Path> getLuaOutputFilePaths(List<Path> tealSourceFilePaths, Path sourceDirPath, Path outputDirPath) {
        return tealSourceFilePaths.stream().map(path -> getLuaOutputFilePath(path, sourceDirPath, outputDirPath)).toList();
    }

    private static Path getLuaOutputFilePath(Path tealSourceFilePath, Path sourceDirPath, Path outputDirPath) {
        String tealSourceFileName = tealSourceFilePath.getFileName().toString();
        String luaOutputFileName = FilenameUtils.removeExtension(tealSourceFileName).concat(".lua");
        Path relativeTealSourceFilePath = sourceDirPath.relativize(tealSourceFilePath);
        Path relativeLuaOutputFilePath = relativeTealSourceFilePath.resolveSibling(luaOutputFileName);
        return outputDirPath.resolve(relativeLuaOutputFilePath);
    }
}
