package com.speed.engramstudio.infrastructure.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EngramProcessManager {

    private static final Logger logger = LoggerFactory.getLogger(EngramProcessManager.class);
    private static final String HEALTH_URL = "http://127.0.0.1:7437/health";
    private static final String ENGRAM_EXE = "engram.exe";

    private volatile Process engramProcess;
    private volatile boolean running = false;

    public CompletableFuture<ProcessStatus> detect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ProcessHandle.allProcesses()
                    .filter(p -> p.info().command().orElse("").toLowerCase().contains("engram"))
                    .findFirst()
                    .ifPresent(ph -> running = true);

                if (!running) {
                    running = checkHealth();
                }

                return new ProcessStatus(running, getPid(), getProcessInfo());
            } catch (Exception e) {
                logger.error("Error detecting Engram process", e);
                return new ProcessStatus(false, -1, "Detection failed: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<ProcessResult> start() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (isRunning()) {
                    return new ProcessResult(false, "Engram is already running");
                }

                ProcessBuilder pb = new ProcessBuilder(ENGRAM_EXE);
                pb.redirectErrorStream(true);
                engramProcess = pb.start();
                running = true;

                // Read output in background
                Thread.startVirtualThread(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(engramProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            logger.info("Engram: {}", line);
                        }
                    } catch (Exception e) {
                        logger.debug("Error reading Engram output", e);
                    }
                });

                // Wait for health
                boolean healthy = waitForHealth(10000);
                if (healthy) {
                    return new ProcessResult(true, "Engram started successfully (PID: " + engramProcess.pid() + ")");
                } else {
                    return new ProcessResult(true, "Engram started but health check timed out");
                }
            } catch (Exception e) {
                logger.error("Error starting Engram", e);
                running = false;
                return new ProcessResult(false, "Failed to start: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<ProcessResult> stop() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!isRunning()) {
                    return new ProcessResult(false, "Engram is not running");
                }

                ProcessHandle.allProcesses()
                    .filter(p -> p.info().command().orElse("").toLowerCase().contains("engram"))
                    .forEach(ph -> {
                        ph.destroyForcibly();
                        logger.info("Killed Engram process PID: {}", ph.pid());
                    });

                running = false;
                engramProcess = null;
                return new ProcessResult(true, "Engram stopped");
            } catch (Exception e) {
                logger.error("Error stopping Engram", e);
                return new ProcessResult(false, "Failed to stop: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<ProcessResult> restart() {
        return stop()
            .thenCompose(r -> {
                if (r.success()) {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    return start();
                }
                return CompletableFuture.completedFuture(r);
            });
    }

    public boolean isRunning() {
        if (running) {
            return checkHealth();
        }
        return false;
    }

    private boolean checkHealth() {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HEALTH_URL))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean waitForHealth(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (checkHealth()) return true;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private long getPid() {
        if (engramProcess != null && engramProcess.isAlive()) {
            return engramProcess.pid();
        }
        return ProcessHandle.allProcesses()
            .filter(p -> p.info().command().orElse("").toLowerCase().contains("engram"))
            .mapToLong(ProcessHandle::pid)
            .findFirst()
            .orElse(-1L);
    }

    private String getProcessInfo() {
        List<String> info = new ArrayList<>();
        ProcessHandle.allProcesses()
            .filter(p -> p.info().command().orElse("").toLowerCase().contains("engram"))
            .forEach(ph -> {
                String cmd = ph.info().command().orElse("unknown");
                info.add("PID " + ph.pid() + " | " + cmd);
            });
        return info.isEmpty() ? "No Engram process found" : String.join("\n", info);
    }

    public record ProcessStatus(boolean running, long pid, String info) {}
    public record ProcessResult(boolean success, String message) {}
}
