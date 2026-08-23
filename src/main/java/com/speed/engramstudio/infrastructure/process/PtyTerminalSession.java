package com.speed.engramstudio.infrastructure.process;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.OutputStream;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Owns one interactive pseudo-terminal session.
 *
 * <p>On Windows pty4j is explicitly asked to use ConPTY. Its documented
 * fallback to WinPTY remains available for machines where ConPTY cannot be
 * loaded, while Unix platforms use their native PTY implementation.</p>
 */
public final class PtyTerminalSession implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(PtyTerminalSession.class);
    private static final int DEFAULT_COLUMNS = 120;
    private static final int DEFAULT_ROWS = 32;

    private final Object lifecycleLock = new Object();
    private volatile PtyProcess process;
    private volatile OutputStream input;

    public void start(List<String> command,
                      Consumer<String> outputConsumer,
                      IntConsumer exitConsumer) throws IOException {
        start(command, "", outputConsumer, exitConsumer);
    }

    public void start(List<String> command,
                      String initialInput,
                      Consumer<String> outputConsumer,
                      IntConsumer exitConsumer) throws IOException {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(outputConsumer, "outputConsumer");
        Objects.requireNonNull(exitConsumer, "exitConsumer");
        if (command.isEmpty()) {
            throw new IllegalArgumentException("PTY command cannot be empty");
        }

        synchronized (lifecycleLock) {
            if (process != null && process.isAlive()) {
                throw new IllegalStateException("A PTY session is already running");
            }

            Map<String, String> environment = new HashMap<>(System.getenv());
            environment.putIfAbsent("TERM", "xterm-256color");
            environment.putIfAbsent("COLORTERM", "truecolor");

            PtyProcessBuilder builder = new PtyProcessBuilder()
                .setCommand(command.toArray(String[]::new))
                .setEnvironment(environment)
                .setInitialColumns(DEFAULT_COLUMNS)
                .setInitialRows(DEFAULT_ROWS)
                .setWindowsAnsiColorEnabled(true)
                .setUseWinConPty(true)
                .setRedirectErrorStream(true);

            PtyProcess started = builder.start();
            process = started;
            input = started.getOutputStream();
            if (initialInput != null && !initialInput.isBlank()) {
                input.write((initialInput + "\r").getBytes(StandardCharsets.UTF_8));
                input.flush();
            }

            Thread.startVirtualThread(() -> pumpOutput(started, outputConsumer, exitConsumer));
        }
    }

    /**
     * Starts a session while preserving the PTY bytes exactly. This is the
     * path used by xterm.js; decoding in Java first can corrupt a Windows
     * console stream before the browser terminal emulator receives it.
     */
    public void startRaw(List<String> command,
                         Consumer<byte[]> outputConsumer,
                         IntConsumer exitConsumer) throws IOException {
        startRaw(command, "", terminalEnvironment(), outputConsumer, exitConsumer);
    }

    public void startRaw(List<String> command,
                         String initialInput,
                         Consumer<byte[]> outputConsumer,
                         IntConsumer exitConsumer) throws IOException {
        startRaw(command, initialInput, terminalEnvironment(), outputConsumer, exitConsumer);
    }

    /**
     * Starts a raw-byte session with an explicit process environment. The
     * supplied map is copied before the process starts, so later changes by
     * the caller cannot affect the running PTY.
     */
    public void startRaw(List<String> command,
                         String initialInput,
                         Map<String, String> environment,
                         Consumer<byte[]> outputConsumer,
                         IntConsumer exitConsumer) throws IOException {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(outputConsumer, "outputConsumer");
        Objects.requireNonNull(exitConsumer, "exitConsumer");
        if (command.isEmpty()) {
            throw new IllegalArgumentException("PTY command cannot be empty");
        }

        synchronized (lifecycleLock) {
            if (process != null && process.isAlive()) {
                throw new IllegalStateException("A PTY session is already running");
            }

            Map<String, String> processEnvironment = new HashMap<>(environment);

            PtyProcess started = new PtyProcessBuilder()
                .setCommand(command.toArray(String[]::new))
                .setEnvironment(processEnvironment)
                .setInitialColumns(DEFAULT_COLUMNS)
                .setInitialRows(DEFAULT_ROWS)
                .setWindowsAnsiColorEnabled(true)
                .setUseWinConPty(true)
                .setRedirectErrorStream(true)
                .start();
            process = started;
            input = started.getOutputStream();
            if (initialInput != null && !initialInput.isBlank()) {
                input.write((initialInput + "\r").getBytes(StandardCharsets.UTF_8));
                input.flush();
            }

            Thread.startVirtualThread(() -> pumpRawOutput(started, outputConsumer, exitConsumer));
        }
    }

    private Map<String, String> terminalEnvironment() {
        Map<String, String> environment = new HashMap<>(System.getenv());
        environment.put("TERM", "xterm-256color");
        environment.put("COLORTERM", "truecolor");
        return environment;
    }

    public boolean isAlive() {
        PtyProcess current = process;
        return current != null && current.isAlive();
    }

    public void write(String text) throws IOException {
        Objects.requireNonNull(text, "text");
        OutputStream currentInput = input;
        if (currentInput == null || !isAlive()) {
            return;
        }
        synchronized (lifecycleLock) {
            currentInput.write(text.getBytes(StandardCharsets.UTF_8));
            currentInput.flush();
        }
    }

    public void resize(int columns, int rows) {
        PtyProcess current = process;
        if (current != null && current.isAlive() && columns > 0 && rows > 0) {
            current.setWinSize(new WinSize(columns, rows));
        }
    }

    public void stop() {
        PtyProcess current;
        synchronized (lifecycleLock) {
            current = process;
            if (current == null) {
                return;
            }
            try {
                current.destroy();
            } catch (Exception e) {
                logger.debug("Could not gracefully stop PTY process", e);
            }
        }

        try {
            if (!current.waitFor(1, java.util.concurrent.TimeUnit.SECONDS) && current.isAlive()) {
                current.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            current.destroyForcibly();
        }
    }

    @Override
    public void close() {
        stop();
        synchronized (lifecycleLock) {
            closeQuietly(input);
            input = null;
            process = null;
        }
    }

    private void pumpOutput(PtyProcess session,
                            Consumer<String> outputConsumer,
                            IntConsumer exitConsumer) {
        int exitCode = -1;
        try (InputStream output = session.getInputStream();
             Reader reader = new InputStreamReader(output,
                 StandardCharsets.UTF_8.newDecoder()
                     .onMalformedInput(CodingErrorAction.REPLACE)
                     .onUnmappableCharacter(CodingErrorAction.REPLACE))) {
            char[] buffer = new char[8192];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                if (count > 0) {
                    outputConsumer.accept(new String(buffer, 0, count));
                }
            }
            exitCode = session.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("PTY output pump interrupted", e);
        } catch (IOException e) {
            if (session.isAlive()) {
                logger.warn("PTY output pump failed", e);
            }
        } finally {
            closeQuietly(session.getOutputStream());
            synchronized (lifecycleLock) {
                if (process == session) {
                    process = null;
                    input = null;
                }
            }
            int result = exitCode;
            exitConsumer.accept(result);
        }
    }

    private void pumpRawOutput(PtyProcess session,
                               Consumer<byte[]> outputConsumer,
                               IntConsumer exitConsumer) {
        int exitCode = -1;
        try (InputStream output = session.getInputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = output.read(buffer)) != -1) {
                if (count > 0) {
                    outputConsumer.accept(java.util.Arrays.copyOf(buffer, count));
                }
            }
            exitCode = session.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("PTY output pump interrupted", e);
        } catch (IOException e) {
            if (session.isAlive()) {
                logger.warn("PTY output pump failed", e);
            }
        } finally {
            closeQuietly(session.getOutputStream());
            synchronized (lifecycleLock) {
                if (process == session) {
                    process = null;
                    input = null;
                }
            }
            int result = exitCode;
            exitConsumer.accept(result);
        }
    }

    private void closeQuietly(OutputStream stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
            logger.debug("Could not close PTY input", e);
        }
    }
}
