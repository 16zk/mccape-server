package com.mccape.common;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CapeService extends AutoCloseable {
    CompletableFuture<Optional<RemoteCapeDescriptor>> findCape(UUID playerId);
    @Override default void close() {}
}
