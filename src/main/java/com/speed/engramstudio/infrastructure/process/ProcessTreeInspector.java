package com.speed.engramstudio.infrastructure.process;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Reads the OS process tree to tell what is running under a terminal shell. */
public final class ProcessTreeInspector {

    private ProcessTreeInspector() {
    }

    /**
     * A single pass over the process table. One snapshot answers many queries,
     * so the tree is enumerated once instead of once per terminal.
     */
    public static Snapshot snapshot() {
        Map<Long, List<Long>> children = new HashMap<>();
        Map<Long, String> commands = new HashMap<>();
        ProcessHandle.allProcesses().forEach(handle -> {
            ProcessHandle.Info info = handle.info();
            commands.put(handle.pid(), info.commandLine()
                .or(info::command)
                .orElse("")
                .toLowerCase(Locale.ROOT));
            handle.parent().ifPresent(parent ->
                children.computeIfAbsent(parent.pid(), key -> new ArrayList<>()).add(handle.pid()));
        });
        return new Snapshot(children, commands);
    }

    public record Snapshot(Map<Long, List<Long>> children, Map<Long, String> commands) {

        /** True when any process below {@code rootPid} was launched from {@code marker}. */
        public boolean hasDescendantMatching(long rootPid, String marker) {
            if (marker == null || marker.isBlank()) return false;
            String needle = marker.toLowerCase(Locale.ROOT);
            Deque<Long> pending = new ArrayDeque<>(children.getOrDefault(rootPid, List.of()));
            while (!pending.isEmpty()) {
                long pid = pending.pop();
                if (commands.getOrDefault(pid, "").contains(needle)) return true;
                pending.addAll(children.getOrDefault(pid, List.of()));
            }
            return false;
        }
    }
}
