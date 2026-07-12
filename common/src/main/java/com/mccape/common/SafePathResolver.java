package com.mccape.common;

import java.nio.file.Path;

public final class SafePathResolver {
    public Path resolve(Path root, String relative) {
        Path result = root.resolve(relative).normalize();
        if (Path.of(relative).isAbsolute() || !result.startsWith(root.normalize()))
            throw new IllegalArgumentException("Path escapes Mc Cape directory");
        return result;
    }
}
