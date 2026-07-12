package com.mccape.common;

public record McCapeConfig(int schemaVersion, boolean enabled, String selectedCapeId,
                           boolean showOwnCape, boolean showRemoteCapes, CapePhysicsMode physicsMode,
                           boolean onlineFeatures, long cacheLimitBytes, long cacheDurationSeconds,
                           String logLevel) {
    public static final int CURRENT_SCHEMA = 2;
    public static McCapeConfig defaults() {
        return new McCapeConfig(CURRENT_SCHEMA, true, null, true, true, CapePhysicsMode.FULL, false,
                64L * 1024 * 1024, 86_400, "NORMAL");
    }
}
