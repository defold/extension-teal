package com.defold.extension.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TealTranspiler implements ILuaTranspiler {
    @Override
    public String getBuildFileResourcePath() {
        return "/tlconfig.lua";
    }

    @Override
    public String getSourceExt() {
        return "tl";
    }

    // 1 warning:
    // 2 warnings:
    private static final Predicate<String> IS_WARNING = Pattern.compile("^\\d+.+warnings?:").asMatchPredicate();

    // 1 error:
    // 4 errors:
    // 2 syntax errors:
    private static final Predicate<String> IS_ERROR = Pattern.compile("^\\d+.+errors?:").asMatchPredicate();

    // foo.tl:10:1: unused variable x: integer
    private static final Pattern ISSUE = Pattern.compile("^(.+):(\\d+):\\d+: (.+)$");

    @Override
    public List<Issue> transpile(File sourceDir, File outputDir) throws Exception {
        // TODO: so far, we rely on a globally installed tl executable. We want to bundle it as a plugin!
        Process process = new ProcessBuilder("tl", "build",
                "--source-dir", sourceDir.toString(),
                "--build-dir", outputDir.toString()).start();
        List<Issue> result = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line = r.readLine();
            Severity severity = null;
            while (line != null) {
                if (IS_WARNING.test(line)) {
                    severity = Severity.WARNING;
                } else if (IS_ERROR.test(line)) {
                    severity = Severity.ERROR;
                } else if (severity != null) {
                    Matcher matcher = ISSUE.matcher(line);
                    if (matcher.matches()) {
                        result.add(new Issue(
                                severity,
                                "/" + matcher.group(1),
                                Integer.parseInt(matcher.group(2)),
                                matcher.group(3)));
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
                    "Compilation failed: " + exitCode + " exit code"));
        }
        return result;
    }
}
