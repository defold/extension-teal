package com.defold.extension.pipeline;

import com.dynamo.bob.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    //        ...    1 | local not_an_int: int = "asd"
    //        ...    9 | local py = "1";
    public static final Pattern LINE = Pattern.compile("^\\s*\\.{3}\\s*(\\d+)\\s*\\|");
    // Match line that highlights the issue in a line above like:
    //        ...      |       ^^
    //        ...      |       ^^^^^
    public static final Pattern ERROR_UNDERLINE = Pattern.compile("^\\s*\\.{3}\\s*\\|\\s*\\^+\\s*$");
    // Match issue message like:
    //        ...      | unknown type int
    //        ...      | in local declaration: foo: expected an array: at index 2: got integer, expected string
    public static final Pattern MESSAGE = Pattern.compile("^\\s*\\.{3}\\s*\\|\\s*(.+)$");

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
        Platform platform = Platform.getHostPlatform();
        String cmd = new File(pluginDir, "teal/plugins/bin/" + platform.getPair() + "/bin/cyan" + platform.getExeSuffixes()[0]).toString();
        Process process = new ProcessBuilder(
                cmd, "build", "--build-dir", outputDir.toString(), "--prune")
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
}
