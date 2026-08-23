package com.speed.engramstudio.infrastructure.process;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Finds configured agent binaries and the command shims exposed on PATH. */
public final class AgentExecutableResolver {

    private AgentExecutableResolver() {
    }

    public static Optional<String> resolve(String commandName, String configuredPath) {
        String normalizedConfiguredPath = normalizeConfiguredExecutable(configuredPath);
        if (!normalizedConfiguredPath.isBlank()) {
            Optional<String> configured = findFile(normalizedConfiguredPath);
            if (configured.isPresent()) return configured;
        }

        Optional<String> directPath = findFile(commandName);
        if (directPath.isPresent()) return directPath;

        Optional<String> userLocalBin = findInUserLocalBin(commandName);
        if (userLocalBin.isPresent()) return userLocalBin;

        return findOnPath(commandName);
    }

    public static Optional<String> findOnPath(String commandName) {
        String pathValue = System.getenv("PATH");
        if (pathValue == null || pathValue.isBlank()) {
            return Optional.empty();
        }

        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        boolean windowsWithoutExtension = isWindows() && !hasExtension(commandName);
        if (!windowsWithoutExtension) {
            candidates.add(commandName);
        }
        if (windowsWithoutExtension) {
            String extensions = System.getenv().getOrDefault("PATHEXT", ".COM;.EXE;.BAT;.CMD");
            for (String extension : extensions.split(";")) {
                if (!extension.isBlank()) candidates.add(commandName + extension.toLowerCase(Locale.ROOT));
            }
            // npm/pnpm frequently exposes PowerShell shims without adding .PS1 to PATHEXT.
            candidates.add(commandName + ".ps1");
            // Keep extensionless Unix-style shims as a last resort; Windows
            // cannot execute these directly through CreateProcess.
            candidates.add(commandName);
        }

        for (String directory : pathValue.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
            if (directory.isBlank()) continue;
            for (String candidate : candidates) {
                Optional<String> found = findFile(Path.of(directory, candidate).toString());
                if (found.isPresent()) return found;
            }
        }
        return Optional.empty();
    }

    /**
     * Adds common per-user CLI locations to a child process environment.
     * Launching the app from an IDE or another desktop process can give the
     * embedded PowerShell a PATH that predates a pnpm or Antigravity install.
     */
    public static void addUserLocalPaths(Map<String, String> environment) {
        if (!isWindows() || environment == null) return;

        String userHome = System.getProperty("user.home", "");
        if (userHome.isBlank()) return;

        List<Path> directories = List.of(
            Path.of(userHome, ".local", "bin"),
            Path.of(userHome, "AppData", "Local", "pnpm"),
            Path.of(userHome, "AppData", "Local", "pnpm", "bin"),
            Path.of(userHome, "AppData", "Local", "agy", "bin"));
        String currentPath = environment.getOrDefault("PATH", "");
        LinkedHashSet<String> paths = new LinkedHashSet<>();
        for (Path directory : directories) {
            if (Files.isDirectory(directory)) paths.add(directory.toString());
        }
        if (!currentPath.isBlank()) {
            for (String directory : currentPath.split(java.util.regex.Pattern.quote(File.pathSeparator))) {
                if (!directory.isBlank()) paths.add(directory);
            }
        }
        environment.put("PATH", String.join(File.pathSeparator, paths));
    }

    private static Optional<String> findInUserLocalBin(String commandName) {
        if (commandName == null || commandName.isBlank()
            || commandName.contains("\\") || commandName.contains("/")) {
            return Optional.empty();
        }

        String userHome = System.getProperty("user.home", "");
        if (userHome.isBlank()) return Optional.empty();

        List<Path> directories = List.of(
            Path.of(userHome, ".local", "bin"),
            // Prefer the pnpm root. A machine can have several generated
            // shims for the same CLI; the root may point to the active
            // installation while the bin shim points to an older one.
            Path.of(userHome, "AppData", "Local", "pnpm"),
            Path.of(userHome, "AppData", "Local", "pnpm", "bin"));
        List<String> extensions = isWindows()
            ? List.of(".exe", ".cmd", ".bat", ".ps1", "")
            : List.of("");

        for (Path directory : directories) {
            for (String extension : extensions) {
                Optional<String> found = findFile(directory.resolve(commandName + extension).toString());
                if (found.isPresent()) return found;
            }
        }
        return Optional.empty();
    }

    public static List<String> buildCommand(String executable, List<String> arguments) {
        List<String> command = new ArrayList<>();
        String lowerCaseExecutable = executable.toLowerCase(Locale.ROOT);
        if (lowerCaseExecutable.endsWith(".ps1")) {
            command.addAll(List.of("powershell.exe", "-NoLogo", "-NoProfile",
                "-ExecutionPolicy", "Bypass", "-File", executable));
        } else if (lowerCaseExecutable.endsWith(".cmd") || lowerCaseExecutable.endsWith(".bat")) {
            String commandLine = quoteWindowsArgument(executable);
            for (String argument : arguments) {
                commandLine += " " + quoteWindowsArgument(argument);
            }
            command.addAll(List.of("cmd.exe", "/d", "/s", "/c", commandLine));
            return command;
        } else {
            command.add(executable);
        }
        command.addAll(arguments);
        return command;
    }

    public static List<String> interactiveShellCommand() {
        if (isWindows()) {
            // Use the Windows PowerShell host requested by the embedded
            // console. Fall back to PowerShell 7 only on machines where the
            // inbox executable is unavailable.
            String powershell = findOnPath("powershell.exe")
                .or(() -> findOnPath("pwsh.exe"))
                .orElse("powershell.exe");
            return List.of(powershell, "-NoLogo", "-NoProfile");
        }
        String shell = System.getenv().getOrDefault("SHELL", "/bin/sh");
        return List.of(shell, "-i");
    }

    /**
     * Starts the shell with the Windows console encoding configured before
     * the first prompt is rendered. Passing the setup as a process argument
     * avoids echoing that implementation detail into the embedded terminal.
     */
    public static List<String> embeddedPowerShellCommand() {
        if (!isWindows()) {
            return interactiveShellCommand();
        }
        String powershell = findOnPath("powershell.exe")
            .or(() -> findOnPath("pwsh.exe"))
            .orElse("powershell.exe");
        return List.of(powershell, "-NoLogo", "-NoProfile", "-NoExit", "-Command",
            powerShellUtf8InitializationCommand());
    }

    public static String powerShellUtf8InitializationCommand() {
        return "Remove-Module PSReadLine -ErrorAction SilentlyContinue; "
            + "chcp 65001 > $null; "
            + "$utf8 = New-Object System.Text.UTF8Encoding($false); "
            + "[Console]::InputEncoding = $utf8; "
            + "[Console]::OutputEncoding = $utf8; "
            + "$OutputEncoding = $utf8";
    }

    public static String buildShellCommand(String executable, List<String> arguments) {
        if (!isWindows()) {
            StringBuilder command = new StringBuilder(quotePosixArgument(executable));
            for (String argument : arguments) {
                command.append(' ').append(quotePosixArgument(argument));
            }
            return command.toString();
        }

        StringBuilder command = isBareCommand(executable)
            ? new StringBuilder(executable)
            : new StringBuilder("& ").append(quotePowerShellArgument(executable));
        for (String argument : arguments) {
            command.append(' ').append(quotePowerShellArgument(argument));
        }
        return command.toString();
    }

    private static Optional<String> findFile(String candidate) {
        try {
            Path path = Path.of(candidate);
            return Files.isRegularFile(path) ? Optional.of(path.toString()) : Optional.empty();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /**
     * Settings historically allowed pasting a PowerShell invocation such as
     * {@code & "C:\\Users\\me\\.local\\bin\\claude.exe"}. Accept the
     * invocation as well as a plain executable path, but return only the path
     * to the process resolver.
     */
    private static String normalizeConfiguredExecutable(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) return "";

        String value = configuredPath.trim();
        int doubleQuoteStart = value.indexOf('"');
        if (doubleQuoteStart >= 0) {
            int doubleQuoteEnd = value.indexOf('"', doubleQuoteStart + 1);
            if (doubleQuoteEnd > doubleQuoteStart) {
                return value.substring(doubleQuoteStart + 1, doubleQuoteEnd).trim();
            }
        }

        int singleQuoteStart = value.indexOf('\'');
        if (singleQuoteStart >= 0) {
            int singleQuoteEnd = value.indexOf('\'', singleQuoteStart + 1);
            if (singleQuoteEnd > singleQuoteStart) {
                return value.substring(singleQuoteStart + 1, singleQuoteEnd).trim();
            }
        }

        if (value.startsWith("&")) value = value.substring(1).trim();
        return value;
    }

    private static boolean hasExtension(String commandName) {
        int slash = Math.max(commandName.lastIndexOf('/'), commandName.lastIndexOf('\\'));
        return commandName.substring(slash + 1).contains(".");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static String quoteWindowsArgument(String argument) {
        if (argument.isEmpty() || argument.chars().anyMatch(Character::isWhitespace)) {
            return "\"" + argument.replace("\"", "\\\"") + "\"";
        }
        return argument;
    }

    private static String quotePowerShellArgument(String argument) {
        return "'" + argument.replace("'", "''") + "'";
    }

    private static boolean isBareCommand(String executable) {
        return executable != null && !executable.isBlank()
            && executable.indexOf('\\') < 0
            && executable.indexOf('/') < 0
            && executable.indexOf(' ') < 0
            && executable.indexOf('\t') < 0
            && executable.indexOf('\'') < 0
            && executable.indexOf('"') < 0;
    }

    private static String quotePosixArgument(String argument) {
        return "'" + argument.replace("'", "'\\''") + "'";
    }
}
