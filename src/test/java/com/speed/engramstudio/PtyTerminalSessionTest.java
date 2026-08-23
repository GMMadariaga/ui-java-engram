package com.speed.engramstudio;

import com.speed.engramstudio.infrastructure.process.PtyTerminalSession;
import com.speed.engramstudio.infrastructure.process.AgentExecutableResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PtyTerminalSessionTest {

    @Test
    void startsInteractiveShellAndExecutesInitialCommand() throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        String initialInput = windows
            ? "Write-Output 'shell-smoke'; exit"
            : "printf 'shell-smoke\\n'; exit";
        StringBuilder output = new StringBuilder();
        CountDownLatch finished = new CountDownLatch(1);

        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.start(AgentExecutableResolver.interactiveShellCommand(), initialInput,
                output::append,
                code -> finished.countDown());
            assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(output.toString()).contains("shell-smoke");
    }

    @Test
    void startsPtyProcessAndPublishesOutput() throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> command = windows
            ? List.of("cmd.exe", "/d", "/c", "echo pty-smoke")
            : List.of("/bin/sh", "-c", "printf 'pty-smoke\\n'");

        StringBuilder output = new StringBuilder();
        AtomicInteger exitCode = new AtomicInteger(-1);
        AtomicReference<Throwable> callbackFailure = new AtomicReference<>();
        CountDownLatch finished = new CountDownLatch(1);

        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.start(command,
                output::append,
                code -> {
                    try {
                        exitCode.set(code);
                    } catch (Throwable failure) {
                        callbackFailure.set(failure);
                    } finally {
                        finished.countDown();
                    }
                });

            assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(callbackFailure.get()).isNull();
        assertThat(output.toString()).contains("pty-smoke");
        assertThat(exitCode).hasValue(0);
    }

    @Test
    void preservesUtf8OutputFromInteractiveProcess() throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> command = windows
            ? List.of("powershell.exe", "-NoLogo", "-NoProfile", "-Command",
                "[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false); Write-Output 'áéñ ✓'")
            : List.of("/bin/sh", "-c", "printf 'áéñ ✓\\n'");

        StringBuilder output = new StringBuilder();
        CountDownLatch finished = new CountDownLatch(1);
        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.start(command, output::append, code -> finished.countDown());
            assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(output.toString()).contains("áéñ ✓");
    }

    @Test
    void preservesRawUtf8BytesForTerminalRenderer() throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> command = windows
            ? List.of("powershell.exe", "-NoLogo", "-NoProfile", "-Command",
                "[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false); Write-Output 'áéñ ✓'")
            : List.of("/bin/sh", "-c", "printf 'áéñ ✓\\n'");

        ByteArrayOutputStream rawOutput = new ByteArrayOutputStream();
        CountDownLatch finished = new CountDownLatch(1);
        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.startRaw(command, bytes -> {
                synchronized (rawOutput) {
                    rawOutput.writeBytes(bytes);
                }
            }, code -> finished.countDown());
            assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(new String(rawOutput.toByteArray(), java.nio.charset.StandardCharsets.UTF_8))
            .contains("áéñ ✓");
    }

    @Test
    void preservesUtf8ThroughEmbeddedInteractivePowerShell() throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

        ByteArrayOutputStream rawOutput = new ByteArrayOutputStream();
        CountDownLatch finished = new CountDownLatch(1);
        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.startRaw(AgentExecutableResolver.embeddedPowerShellCommand(), bytes -> {
                synchronized (rawOutput) {
                    rawOutput.writeBytes(bytes);
                }
            }, code -> finished.countDown());

            Thread.sleep(900);
            session.write("Write-Output 'áéñ ✓'; exit\r");
            assertThat(finished.await(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(new String(rawOutput.toByteArray(), java.nio.charset.StandardCharsets.UTF_8))
            .contains("áéñ ✓");
    }

    @Test
    void executesOpenCodeUsingTheBarePowerShellCommand() throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("win")
            || AgentExecutableResolver.findOnPath("opencode").isEmpty()) return;

        ByteArrayOutputStream rawOutput = new ByteArrayOutputStream();
        CountDownLatch finished = new CountDownLatch(1);
        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.startRaw(AgentExecutableResolver.embeddedPowerShellCommand(), bytes -> {
                synchronized (rawOutput) {
                    rawOutput.writeBytes(bytes);
                }
            }, code -> finished.countDown());

            Thread.sleep(900);
            session.write("opencode --version; exit\r");
            assertThat(finished.await(15, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(new String(rawOutput.toByteArray(), java.nio.charset.StandardCharsets.UTF_8))
            .contains("1.17.7");
    }

    @Test
    void executesOpenCodeUsingTheJavaResolvedLauncher() throws Exception {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

        String executable = AgentExecutableResolver.resolve("opencode", "").orElse(null);
        if (executable == null) return;

        String command = AgentExecutableResolver.buildShellCommand(executable, List.of("--version"));
        ByteArrayOutputStream rawOutput = new ByteArrayOutputStream();
        CountDownLatch finished = new CountDownLatch(1);
        try (PtyTerminalSession session = new PtyTerminalSession()) {
            session.startRaw(AgentExecutableResolver.embeddedPowerShellCommand(), bytes -> {
                synchronized (rawOutput) {
                    rawOutput.writeBytes(bytes);
                }
            }, code -> finished.countDown());

            Thread.sleep(900);
            session.write(command + "; exit\r");
            assertThat(finished.await(15, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(new String(rawOutput.toByteArray(), java.nio.charset.StandardCharsets.UTF_8))
            .contains("1.17.7");
    }

    @Test
    void addsAntigravityDirectoryToEmbeddedPowerShellPath() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) return;

        Path agy = Path.of(System.getProperty("user.home"), "AppData", "Local",
            "agy", "bin", "agy.exe");
        if (!Files.isRegularFile(agy)) return;

        Map<String, String> environment = new HashMap<>();
        environment.put("PATH", "C:\\Windows\\System32");
        AgentExecutableResolver.addUserLocalPaths(environment);
        assertThat(environment.get("PATH")).contains(agy.getParent().toString());
    }

}
