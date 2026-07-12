package com.mccape.common;

public final class CapePhysicsSolver {
    public CapePhysicsState applyMode(CapePhysicsState input, CapePhysicsMode mode, float intensity) {
        float safeIntensity = clamp(finite(intensity), 0f, 2f);
        float lift = finite(input.lift()), swing = finite(input.swing()), sideways = finite(input.sideways());
        return switch (mode) {
            case DISABLED -> new CapePhysicsState(0f, 0f, 0f);
            case SIMPLIFIED -> new CapePhysicsState(
                    clamp(lift * 0.45f * safeIntensity, -3f, 14f),
                    clamp(swing * 0.35f * safeIntensity, 0f, 35f),
                    clamp(sideways * 0.25f * safeIntensity, -6f, 6f));
            case FULL -> new CapePhysicsState(
                    clamp(lift * safeIntensity, -6f, 32f),
                    clamp(swing * safeIntensity, 0f, 150f),
                    clamp(sideways * safeIntensity, -20f, 20f));
        };
    }
    private float finite(float value) { return Float.isFinite(value) ? value : 0f; }
    private float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
}
