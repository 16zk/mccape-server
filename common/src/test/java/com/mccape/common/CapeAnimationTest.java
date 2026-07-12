package com.mccape.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapeAnimationTest {
    private final CapeAnimationClock clock = new CapeAnimationClock();
    @Test void loopsByElapsedTimeNotFrames() {
        CapeAnimation a = new CapeAnimation(64, 32, 4, 100, true);
        assertEquals(0, clock.frameAt(a, 0)); assertEquals(3, clock.frameAt(a, 399));
        assertEquals(0, clock.frameAt(a, 400)); assertEquals(2, clock.frameAt(a, 10_600));
    }
    @Test void nonLoopingStopsAtLastFrame() {
        CapeAnimation a = new CapeAnimation(64, 32, 3, 50, false);
        assertEquals(2, clock.frameAt(a, 9999)); assertEquals(0, clock.frameAt(a, -50));
    }
    @Test void rejectsInvalidAnimation() {
        assertThrows(IllegalArgumentException.class, () -> new CapeAnimation(64, 32, 0, 100, true));
        assertThrows(IllegalArgumentException.class, () -> new CapeAnimation(64, 32, 2, 0, true));
    }
}
