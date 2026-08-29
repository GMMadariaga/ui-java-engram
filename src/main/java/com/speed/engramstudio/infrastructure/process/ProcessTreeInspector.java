package com.speed.engramstudio.infrastructure.process;

import java.util.Locale;

/** Reads the OS process tree to tell what is running under a terminal shell. */
public final class ProcessTreeInspector {

    private ProcessTreeInspector() {
    }

    /**
     * True when any process below {@code rootPid} was launched from {@code marker}.
     *
     * <p>Only that subtree is read. Reading process details is a native call
     * per process, so walking the whole process table costs seconds on a busy
     * machine while a shell subtree costs milliseconds.</p>
     */
    public static boolean hasDescendantMatching(long rootPid, String marker) {
        if (marker == null || marker.isBlank()) return false;
        String needle = marker.toLowerCase(Locale.ROOT);
        return ProcessHandle.of(rootPid).stream()
            .flatMap(ProcessHandle::descendants)
            .anyMatch(handle -> commandOf(handle).contains(needle));
    }

    /** Windows reports no command line, so the executable path is the fallback. */
    private static String commandOf(ProcessHandle handle) {
        ProcessHandle.Info info = handle.info();
        return info.commandLine().or(info::command).orElse("").toLowerCase(Locale.ROOT);
    }
}
