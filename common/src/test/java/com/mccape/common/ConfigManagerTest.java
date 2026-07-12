package com.mccape.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class ConfigManagerTest {
    @TempDir Path temp;

    @Test void createsAndReloadsDefaults() throws Exception {
        ConfigManager manager = new ConfigManager(temp);
        assertEquals(McCapeConfig.defaults(), manager.load());
        assertEquals(McCapeConfig.defaults(), manager.load());
        assertTrue(Files.isDirectory(temp.resolve("capes")));
    }

    @Test void recoversCorruptJson() throws Exception {
        Files.writeString(temp.resolve("config.json"), "{broken");
        McCapeConfig config = new ConfigManager(temp).load();
        assertTrue(config.enabled());
        assertTrue(Files.list(temp).anyMatch(path -> path.getFileName().toString().startsWith("config.json.broken-")));
    }

    @Test void rejectsTraversal() {
        assertThrows(IllegalArgumentException.class, () -> new SafePathResolver().resolve(temp, "../escape.png"));
        assertThrows(IllegalArgumentException.class, () -> new CapeDefinition(CapeId.create(), "x", "../x.png",
                CapeType.STATIC, new CapeMetadata(1, 1, 64, 32, "0".repeat(64)), null));
    }

    @Test void rejectsIdCollision() {
        CapeLibrary library = new CapeLibrary();
        CapeDefinition cape = new CapeDefinition(new CapeId("same"), "Cape", "capes/x.png", CapeType.STATIC,
                new CapeMetadata(1, 1, 64, 32, "0".repeat(64)), null);
        library.add(cape);
        assertThrows(IllegalArgumentException.class, () -> library.add(cape));
    }
}
