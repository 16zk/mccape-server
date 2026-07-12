package com.mccape.common;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public final class RemoteCapeService implements CapeService {
    private final URI baseUri;
    private final HttpClient http;
    private final Gson gson = new Gson();
    private final RemoteCapeCache cache;
    private final Map<UUID, CompletableFuture<Optional<RemoteCapeDescriptor>>> inFlight = new ConcurrentHashMap<>();
    private volatile int consecutiveFailures;
    private volatile long circuitOpenUntil;

    public RemoteCapeService(URI baseUri, int maxCacheEntries) {
        if (!baseUri.toString().endsWith("/")) baseUri = URI.create(baseUri + "/");
        if (!"https".equalsIgnoreCase(baseUri.getScheme()) && !isLocal(baseUri)) throw new IllegalArgumentException("API must use HTTPS");
        this.baseUri = baseUri; this.cache = new RemoteCapeCache(maxCacheEntries);
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }
    @Override public CompletableFuture<Optional<RemoteCapeDescriptor>> findCape(UUID playerId) {
        long now = System.currentTimeMillis(); Optional<RemoteCapeDescriptor> hit = cache.get(playerId, now);
        if (hit.isPresent()) return CompletableFuture.completedFuture(hit);
        if (now < circuitOpenUntil) return CompletableFuture.completedFuture(Optional.empty());
        return inFlight.computeIfAbsent(playerId, this::request).whenComplete((v, e) -> inFlight.remove(playerId));
    }
    private CompletableFuture<Optional<RemoteCapeDescriptor>> request(UUID playerId) {
        HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("api/v1/players/" + playerId + "/cape"))
                .timeout(Duration.ofSeconds(8)).header("Accept", "application/json").GET().build();
        return http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 404) { success(); return Optional.<RemoteCapeDescriptor>empty(); }
                    if (response.statusCode() == 429 || response.statusCode() >= 500) throw new CompletionException(new IllegalStateException("Remote API unavailable: " + response.statusCode()));
                    if (response.statusCode() != 200) throw new CompletionException(new IllegalStateException("Unexpected API response: " + response.statusCode()));
                    RemoteCapeDescriptor descriptor = gson.fromJson(response.body(), RemoteCapeDescriptor.class);
                    cache.put(descriptor, System.currentTimeMillis()); success(); return Optional.of(descriptor);
                }).exceptionally(error -> { failure(); return Optional.<RemoteCapeDescriptor>empty(); });
    }
    private void success() { consecutiveFailures = 0; circuitOpenUntil = 0; }
    private void failure() { if (++consecutiveFailures >= 3) { circuitOpenUntil = System.currentTimeMillis() + 30_000; consecutiveFailures = 0; } }
    private static boolean isLocal(URI uri) { return "http".equalsIgnoreCase(uri.getScheme()) && ("localhost".equals(uri.getHost()) || "127.0.0.1".equals(uri.getHost())); }
}
