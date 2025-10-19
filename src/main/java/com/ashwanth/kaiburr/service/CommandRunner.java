package com.ashwanth.kaiburr.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class CommandRunner {

    public static class Result {
        public final int exitCode; public final String stdout; public final String stderr;
        public Result(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode; this.stdout = stdout; this.stderr = stderr;
        }
    }

    public Result run(String command, Duration timeout) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
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
