package com.ashwanth.kaiburr.security;

import java.util.List;

public class CommandPolicy {
    private static final List<String> ALLOWED = List.of(
        "echo",
        "java -version",
        "mvn -v"
    );

    public static boolean allowed(String cmd) {
        String c = cmd.trim().toLowerCase();
        return ALLOWED.stream().anyMatch(c::startsWith);
    }
}
