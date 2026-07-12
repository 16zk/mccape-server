package com.mccape.common;

public record CapeImageInfo(int width, int height, boolean hasAlpha, long fileSize, String sha256) {}
