package com.mccape.common;

public final class CapeAnimationClock {
    public int frameAt(CapeAnimation animation, long elapsedMillis) {
        if (elapsedMillis < 0) elapsedMillis = 0;
        long index = elapsedMillis / animation.frameDurationMs();
        if (animation.loop()) return (int) (index % animation.frameCount());
        return (int) Math.min(index, animation.frameCount() - 1L);
    }
}
