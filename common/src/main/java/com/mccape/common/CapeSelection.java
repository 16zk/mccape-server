package com.mccape.common;

public record CapeSelection(String selectedCapeId) {
    public static CapeSelection none() { return new CapeSelection(null); }
}
