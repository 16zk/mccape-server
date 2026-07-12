package com.mccape.common;

import java.net.URI;
import java.util.UUID;

public record RemoteCapeDescriptor(UUID playerId, String capeId, URI textureUri, String sha256,
                                   long expiresAtEpochMillis, String etag) {
    public RemoteCapeDescriptor {
        if (playerId == null || capeId == null || capeId.isBlank() || textureUri == null)
            throw new IllegalArgumentException("Incomplete remote cape descriptor");
        if (!"https".equalsIgnoreCase(textureUri.getScheme()) && !isLocalDevelopment(textureUri))
            throw new IllegalArgumentException("Remote cape texture must use HTTPS");
        if (sha256 == null || !sha256.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid SHA-256");
    }
    private static boolean isLocalDevelopment(URI uri) {
        String host = uri.getHost(); return "http".equalsIgnoreCase(uri.getScheme()) && ("localhost".equals(host) || "127.0.0.1".equals(host));
    }
}
