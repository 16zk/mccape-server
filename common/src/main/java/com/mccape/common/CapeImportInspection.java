package com.mccape.common;

public record CapeImportInspection(CapeImageInfo image, CapeAnimation animation) {
    public boolean animated() { return animation != null && animation.frameCount() > 1; }
}
