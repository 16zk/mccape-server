package com.mccape.common;

import java.nio.file.Path;
import java.util.Objects;

public record CapeDefinition(CapeId id, String name, String relativePath, CapeType type,
                             CapeMetadata metadata, CapeAnimation animation) {
    public CapeDefinition {
        Objects.requireNonNull(id); Objects.requireNonNull(type); Objects.requireNonNull(metadata);
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Cape name cannot be blank");
        if (relativePath == null || Path.of(relativePath).isAbsolute() || relativePath.contains(".."))
            throw new IllegalArgumentException("Cape path must be safe and relative");
        if (type == CapeType.ANIMATED && animation == null) throw new IllegalArgumentException("Animation metadata required");
    }
}
