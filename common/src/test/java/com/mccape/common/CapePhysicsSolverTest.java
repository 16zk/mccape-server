package com.mccape.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CapePhysicsSolverTest {
    private final CapePhysicsSolver solver = new CapePhysicsSolver();

    @Test void disabledIsStill() {
        assertEquals(new CapePhysicsState(0, 0, 0), solver.applyMode(new CapePhysicsState(10, 50, 8), CapePhysicsMode.DISABLED, 1));
    }
    @Test void simplifiedIsBounded() {
        CapePhysicsState state = solver.applyMode(new CapePhysicsState(1000, 1000, -1000), CapePhysicsMode.SIMPLIFIED, 2);
        assertEquals(14, state.lift()); assertEquals(35, state.swing()); assertEquals(-6, state.sideways());
    }
    @Test void rejectsNonFiniteInputsWithoutNaN() {
        CapePhysicsState state = solver.applyMode(new CapePhysicsState(Float.NaN, Float.POSITIVE_INFINITY, 2), CapePhysicsMode.FULL, Float.NaN);
        assertTrue(Float.isFinite(state.lift())); assertTrue(Float.isFinite(state.swing())); assertTrue(Float.isFinite(state.sideways()));
    }
    @Test void fullModeClampsTeleportLikeSpike() {
        CapePhysicsState state = solver.applyMode(new CapePhysicsState(99999, 99999, 99999), CapePhysicsMode.FULL, 1);
        assertEquals(new CapePhysicsState(32, 150, 20), state);
    }
}
