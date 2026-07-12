package com.mccape.common;

import java.util.Objects;
import java.util.UUID;

public record CapeId(String value) {
    public CapeId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("Cape id cannot be blank");
    }

    public static CapeId create() { return new CapeId(UUID.randomUUID().toString()); }
}
