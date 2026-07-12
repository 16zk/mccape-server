package com.mccape.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthService {
    private static final long CHALLENGE_TTL_MS = 120_000;
    private static final long TOKEN_TTL_MS = 15 * 60_000;
    private final SecureRandom random = new SecureRandom();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final ObjectMapper json = new ObjectMapper();
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public ChallengeResponse challenge() {
        cleanup();
        String id = randomToken(24), serverId = randomHex(20);
        long expires = System.currentTimeMillis() + CHALLENGE_TTL_MS;
        challenges.put(id, new Challenge(serverId, expires));
        return new ChallengeResponse(id, serverId, expires);
    }

    public SessionResponse complete(String challengeId, String username) throws Exception {
        cleanup();
        Challenge challenge = challenges.remove(challengeId);
        if (challenge == null || challenge.expiresAt() < System.currentTimeMillis()) throw new SecurityException("Challenge expired");
        String uri = "https://sessionserver.mojang.com/session/minecraft/hasJoined?username="
                + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&serverId=" + URLEncoder.encode(challenge.serverId(), StandardCharsets.UTF_8);
        HttpResponse<String> response = http.send(HttpRequest.newBuilder(URI.create(uri)).timeout(Duration.ofSeconds(10)).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new SecurityException("Minecraft session proof failed");
        JsonNode profile = json.readTree(response.body());
        UUID player = undashedUuid(profile.path("id").asText());
        String token = randomToken(32); long expires = System.currentTimeMillis() + TOKEN_TTL_MS;
        sessions.put(token, new Session(player, expires));
        return new SessionResponse(token, player, expires);
    }

    public UUID authenticate(String authorization) {
        cleanup();
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new SecurityException("Bearer token missing");
        Session session = sessions.get(authorization.substring(7));
        if (session == null || session.expiresAt() < System.currentTimeMillis()) throw new SecurityException("Session expired");
        return session.player();
    }
    private void cleanup() {
        long now = System.currentTimeMillis(); challenges.entrySet().removeIf(e -> e.getValue().expiresAt() < now); sessions.entrySet().removeIf(e -> e.getValue().expiresAt() < now);
    }
    private String randomToken(int bytes) { byte[] value = new byte[bytes]; random.nextBytes(value); return Base64.getUrlEncoder().withoutPadding().encodeToString(value); }
    private String randomHex(int bytes) { return HexFormat.of().formatHex(Base64.getUrlDecoder().decode(randomToken(bytes))); }
    private UUID undashedUuid(String value) { return UUID.fromString(value.replaceFirst("([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})", "$1-$2-$3-$4-$5")); }
    private record Challenge(String serverId, long expiresAt) {}
    private record Session(UUID player, long expiresAt) {}
    public record ChallengeResponse(String challengeId, String serverId, long expiresAt) {}
    public record SessionResponse(String token, UUID playerId, long expiresAt) {}
    public record CompleteRequest(String challengeId, String username) {}
}
