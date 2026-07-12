package com.mccape.common;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class LocalCapeService implements CapeService {
    @Override public CompletableFuture<Optional<RemoteCapeDescriptor>> findCape(UUID playerId) {
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
