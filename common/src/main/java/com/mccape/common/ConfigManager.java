package com.mccape.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path root;
    private final Path configFile;

    public ConfigManager(Path root) { this.root = root; this.configFile = root.resolve("config.json"); }

    public void initializeDirectories() throws IOException {
        for (String name : new String[]{"capes", "imports", "exports", "cache", "backups"})
            Files.createDirectories(root.resolve(name));
    }

    public McCapeConfig load() throws IOException {
        initializeDirectories();
        if (Files.notExists(configFile)) { McCapeConfig value = McCapeConfig.defaults(); save(value); return value; }
        try {
            McCapeConfig value = GSON.fromJson(Files.readString(configFile, StandardCharsets.UTF_8), McCapeConfig.class);
            if (value == null || value.schemaVersion() != McCapeConfig.CURRENT_SCHEMA)
                return migrate(value);
            return value;
        } catch (RuntimeException exception) {
            Files.move(configFile, configFile.resolveSibling("config.json.broken-" + Instant.now().toEpochMilli()));
            McCapeConfig value = McCapeConfig.defaults(); save(value); return value;
        }
    }

    private McCapeConfig migrate(McCapeConfig old) throws IOException {
        new BackupManager(root.resolve("backups")).backup(configFile);
        McCapeConfig value = old == null ? McCapeConfig.defaults() : new McCapeConfig(
                McCapeConfig.CURRENT_SCHEMA, old.enabled(), old.selectedCapeId(), old.showOwnCape(),
                old.showRemoteCapes(), old.physicsMode() == null ? CapePhysicsMode.FULL : old.physicsMode(), old.onlineFeatures(), old.cacheLimitBytes(),
                old.cacheDurationSeconds(), old.logLevel());
        save(value); return value;
    }

    public void save(McCapeConfig config) throws IOException { AtomicFileWriter.writeUtf8(configFile, GSON.toJson(config)); }
}
