package com.speed.engramstudio;

import javafx.application.Application;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void shouldHaveMainMethod() {
        assertDoesNotThrow(() -> {
            Main.class.getMethod("main", String[].class);
        });
    }

    @Test
    void shouldKeepNativeLauncherSeparateFromJavaFxApplication() {
        assertFalse(Application.class.isAssignableFrom(Main.class));
        assertTrue(Application.class.isAssignableFrom(EngramStudioApplication.class));
    }
}
