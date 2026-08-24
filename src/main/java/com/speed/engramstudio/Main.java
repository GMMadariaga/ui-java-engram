package com.speed.engramstudio;

/** Native launcher used by Maven and jpackage. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        EngramStudioApplication.launch(EngramStudioApplication.class, args);
    }
}
