package com.mccape.server;

import io.javalin.Javalin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class McCapeServerTest {
    @TempDir Path temp;
    @Test void uploadLookupTextureAndDelete() throws Exception {
        Javalin app = McCapeServer.start(new ServerConfig(0, temp, true));
        try {
            String base = "http://127.0.0.1:" + app.port(); UUID player = UUID.randomUUID();
            HttpClient http = HttpClient.newHttpClient();
            BufferedImage image = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
            Path png = temp.resolve("test.png"); ImageIO.write(image, "PNG", png.toFile());
            HttpResponse<String> upload = http.send(HttpRequest.newBuilder(URI.create(base + "/api/v1/capes"))
                    .header("Content-Type", "image/png").header("X-Dev-Player", player.toString())
                    .POST(HttpRequest.BodyPublishers.ofFile(png)).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(201, upload.statusCode()); assertTrue(upload.body().contains("capeId"));
            HttpResponse<String> descriptor = http.send(HttpRequest.newBuilder(URI.create(base + "/api/v1/players/" + player + "/cape")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, descriptor.statusCode()); assertTrue(descriptor.body().contains(player.toString()));
            String textureUrl = descriptor.body().replaceAll(".*\\\"textureUri\\\":\\\"([^\\\"]+)\\\".*", "$1");
            assertEquals(200, http.send(HttpRequest.newBuilder(URI.create(textureUrl)).GET().build(), HttpResponse.BodyHandlers.discarding()).statusCode());
            assertEquals(204, http.send(HttpRequest.newBuilder(URI.create(base + "/api/v1/players/" + player + "/cape"))
                    .header("X-Dev-Player", player.toString()).DELETE().build(), HttpResponse.BodyHandlers.discarding()).statusCode());
        } finally { app.stop(); }
    }
    @Test void uploadIsUnauthorizedWhenDevelopmentProviderIsDisabled() throws Exception {
        Javalin app = McCapeServer.start(new ServerConfig(0, temp, false));
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + app.port() + "/api/v1/capes"))
                    .header("Content-Type", "image/png").POST(HttpRequest.BodyPublishers.ofByteArray(new byte[16])).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(401, response.statusCode());
        } finally { app.stop(); }
    }
}
