package com.speed.engramstudio.infrastructure.process;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/** Resolves the Engram command without relying on a user-specific hardcoded path. */
public final class EngramExecutableResolver {

    private EngramExecutableResolver() {
    }

    public static Optional<String> resolve() {
        Set<String> candidates = new LinkedHashSet<>();
        String configured = System.getProperty("engram.executable", "").trim();
        if (!configured.isEmpty()) candidates.add(configured);

        String userHome = System.getProperty("user.home", "");
        if (!userHome.isBlank()) {
            candidates.add(userHome + "\\go\\bin\\engram.exe");
            candidates.add(userHome + "\\go\\bin\\engram");
        }

        String goBin = System.getenv("GOBIN");
        if (goBin != null && !goBin.isBlank()) {
            candidates.add(goBin + File.separator + "engram.exe");
            candidates.add(goBin + File.separator + "engram");
        }

        // Bare commands allow Windows PATH and Unix-like environments to resolve them.
        candidates.add("engram.exe");
        candidates.add("engram");

        for (String candidate : candidates) {
            if (isRunnable(candidate)) return Optional.of(candidate);
        }
        return Optional.empty();
    }

    private static boolean isRunnable(String candidate) {
        if (candidate.contains("\\") || candidate.contains("/")) {
            if (!new File(candidate).isFile()) return false;
        }
        try {
            Process process = new ProcessBuilder(candidate, "version")
                .redirectErrorStream(true)
                .start();
            boolean finished = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) process.destroyForcibly();
            return finished;
        } catch (Exception ignored) {
            return false;
        }
    }
}
