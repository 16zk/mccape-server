package com.mccape.common;

import java.util.List;

public record PixelEdit(List<PixelChange> changes) {
    public PixelEdit { changes = List.copyOf(changes); }
}
