package com.speed.engramstudio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void shouldHaveMainMethod() {
        assertDoesNotThrow(() -> {
            Main.class.getMethod("main", String[].class);
        });
    }
}