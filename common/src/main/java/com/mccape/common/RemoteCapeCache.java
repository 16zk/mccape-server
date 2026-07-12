package com.mccape.common;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class RemoteCapeCache {
    private final int maxEntries;
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();
    public RemoteCapeCache(int maxEntries) { if (maxEntries < 1) throw new IllegalArgumentException(); this.maxEntries = maxEntries; }
    public Optional<RemoteCapeDescriptor> get(UUID player, long now) {
        Entry entry = entries.get(player);
        if (entry == null || entry.value().expiresAtEpochMillis() <= now) { entries.remove(player); return Optional.empty(); }
        entry.touch(now); return Optional.of(entry.value());
    }
    public synchronized void put(RemoteCapeDescriptor descriptor, long now) {
        entries.put(descriptor.playerId(), new Entry(descriptor, now));
        while (entries.size() > maxEntries) entries.entrySet().stream()
                .min(Comparator.comparingLong(e -> e.getValue().lastAccess)).ifPresent(e -> entries.remove(e.getKey()));
    }
    public void invalidate(UUID player) { entries.remove(player); }
    public int size() { return entries.size(); }
    private static final class Entry {
        private final RemoteCapeDescriptor value; private volatile long lastAccess;
        private Entry(RemoteCapeDescriptor value, long lastAccess) { this.value = value; this.lastAccess = lastAccess; }
        private RemoteCapeDescriptor value() { return value; } private void touch(long now) { lastAccess = now; }
    }
}
