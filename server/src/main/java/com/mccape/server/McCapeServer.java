package com.mccape.server;

import com.google.gson.Gson;
import com.mccape.common.*;
import io.javalin.Javalin;
import io.javalin.http.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class McCapeServer {
    private static final Gson GSON = new Gson();
    public static void main(String[] args) throws Exception { start(ServerConfig.environment()); }
    public static Javalin start(ServerConfig config) throws Exception {
        CapeStore store = new CapeStore(config.dataDirectory());
        Javalin app = Javalin.create(c -> c.http.maxRequestSize = 5L * 1024 * 1024).start(config.port());
        app.events(events -> events.serverStopped(store::close));
        app.get("/health", ctx -> ctx.json(Map.of("status", "ok")));
        app.post("/api/v1/capes", ctx -> upload(ctx, config, store));
        app.get("/api/v1/players/{uuid}/cape", ctx -> descriptor(ctx, store));
        app.get("/api/v1/capes/{id}/texture", ctx -> texture(ctx, store));
        app.delete("/api/v1/players/{uuid}/cape", ctx -> remove(ctx, config, store));
        return app;
    }
    private static UUID authenticate(Context ctx, ServerConfig config) {
        if (!config.insecureDevelopmentAuth()) throw new UnauthorizedResponse("No production identity provider configured");
        String value = ctx.header("X-Dev-Player"); if (value == null) throw new UnauthorizedResponse("Development header missing");
        return UUID.fromString(value);
    }
    private static void upload(Context ctx, ServerConfig config, CapeStore store) throws Exception {
        UUID player = authenticate(ctx, config);
        if (!"image/png".equalsIgnoreCase(ctx.contentType())) throw new UnsupportedMediaTypeResponse("Expected image/png");
        Path temporary = Files.createTempFile(config.dataDirectory(), "upload-", ".png");
        try {
            Files.write(temporary, ctx.bodyAsBytes()); CapeImportInspection inspection = new CapeFileInspector().inspect(temporary);
            CapeStore.StoredCape saved = store.save(player, temporary, inspection.image().sha256());
            ctx.status(201).header("ETag", saved.sha256()).json(Map.of("capeId", saved.id(), "sha256", saved.sha256()));
        } finally { Files.deleteIfExists(temporary); }
    }
    private static void descriptor(Context ctx, CapeStore store) throws Exception {
        UUID player = UUID.fromString(ctx.pathParam("uuid")); CapeStore.StoredCape cape = store.byPlayer(player).orElseThrow(NotFoundResponse::new);
        String base = ctx.scheme() + "://" + ctx.host();
        ctx.header("ETag", cape.sha256()).header("Cache-Control", "public, max-age=300").json(Map.of(
                "playerId", player.toString(), "capeId", cape.id(), "textureUri", base + "/api/v1/capes/" + cape.id() + "/texture",
                "sha256", cape.sha256(), "expiresAtEpochMillis", Instant.now().plusSeconds(300).toEpochMilli(), "etag", cape.sha256()));
    }
    private static void texture(Context ctx, CapeStore store) throws Exception {
        CapeStore.StoredCape cape = store.byId(ctx.pathParam("id")).orElseThrow(NotFoundResponse::new);
        ctx.contentType("image/png").header("ETag", cape.sha256()).header("Cache-Control", "public, max-age=3600").result(Files.newInputStream(store.texture(cape)));
    }
    private static void remove(Context ctx, ServerConfig config, CapeStore store) throws Exception {
        UUID requested = UUID.fromString(ctx.pathParam("uuid")), authenticated = authenticate(ctx, config);
        if (!requested.equals(authenticated)) throw new ForbiddenResponse(); store.delete(requested); ctx.status(204);
    }
}
