package com.mccape.common;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CapeVisibilityPolicy {
    private final Set<UUID> hiddenPlayers = ConcurrentHashMap.newKeySet();
    public boolean mayDisplay(UUID player, boolean onlineEnabled, boolean showRemote) {
        return onlineEnabled && showRemote && player != null && !hiddenPlayers.contains(player);
    }
    public void hide(UUID player) { hiddenPlayers.add(player); }
    public void show(UUID player) { hiddenPlayers.remove(player); }
}
