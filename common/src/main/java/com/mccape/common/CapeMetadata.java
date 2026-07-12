package com.mccape.common;

public record CapeMetadata(long createdAt, long modifiedAt, int width, int height, String sha256) {
    public CapeMetadata {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid cape dimensions");
        if (sha256 == null || !sha256.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid SHA-256");
    }
}
