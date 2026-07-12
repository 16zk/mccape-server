package com.mccape.common;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RemoteCapeArchitectureTest {
    private RemoteCapeDescriptor descriptor(UUID id, long expiry) {
        return new RemoteCapeDescriptor(id, "cape", URI.create("https://example.invalid/cape.png"), "a".repeat(64), expiry, "etag");
    }
    @Test void cacheExpiresAndIsBounded() {
        RemoteCapeCache cache = new RemoteCapeCache(1); UUID one = UUID.randomUUID(), two = UUID.randomUUID();
        cache.put(descriptor(one, 200), 10); assertTrue(cache.get(one, 100).isPresent()); assertTrue(cache.get(one, 201).isEmpty());
        cache.put(descriptor(one, 500), 20); cache.put(descriptor(two, 500), 30); assertEquals(1, cache.size());
    }
    @Test void visibilityHonorsPrivacyAndSettings() {
        UUID player = UUID.randomUUID(); CapeVisibilityPolicy policy = new CapeVisibilityPolicy();
        assertTrue(policy.mayDisplay(player, true, true)); policy.hide(player); assertFalse(policy.mayDisplay(player, true, true));
        policy.show(player); assertFalse(policy.mayDisplay(player, false, true));
    }
    @Test void productionEndpointsRequireHttps() {
        assertThrows(IllegalArgumentException.class, () -> new RemoteCapeService(URI.create("http://example.com/"), 10));
        assertDoesNotThrow(() -> new RemoteCapeService(URI.create("http://localhost:8080/"), 10));
    }
}
