package com.ashwanth.kaiburr.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandRunner {
    public static class Result {
        public final int exitCode; public final String stdout; public final String stderr;
        public Result(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode; this.stdout = stdout; this.stderr = stderr;
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private static List<String> buildCommand(String command) {
        if (isWindows()) {
            // Windows container or host
            return List.of("cmd.exe", "/c", command);
        } else {
            // Linux container (Alpine/Temurin)
            return List.of("sh", "-lc", command);
        }
    }

    public Result run(String command, Duration timeout) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(buildCommand(command));
        pb.redirectErrorStream(false);
        Process p = pb.start();
        boolean finished = p.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            p.destroyForcibly();
            return new Result(124, "", "Timed out");
        }
        String stdout = readAll(p.getInputStream());
        String stderr = readAll(p.getErrorStream());
        return new Result(p.exitValue(), stdout, stderr);
    }

    private String readAll(java.io.InputStream is) throws Exception {
        var sb = new StringBuilder();
        try (var br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
