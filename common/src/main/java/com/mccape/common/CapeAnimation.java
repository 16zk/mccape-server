package com.mccape.common;

public record CapeAnimation(int frameWidth, int frameHeight, int frameCount, long frameDurationMs, boolean loop) {
    public CapeAnimation {
        if (frameWidth <= 0 || frameHeight <= 0 || frameCount <= 0 || frameDurationMs <= 0)
            throw new IllegalArgumentException("Invalid animation properties");
    }
}
