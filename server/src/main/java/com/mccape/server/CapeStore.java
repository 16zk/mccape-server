package com.mccape.server;

import com.mccape.common.*;
import java.nio.file.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public final class CapeStore implements AutoCloseable {
    private final Connection database;
    private final Path textures;
    public CapeStore(Path root) throws Exception {
        Files.createDirectories(root); textures = root.resolve("textures"); Files.createDirectories(textures);
        database = DriverManager.getConnection("jdbc:h2:file:" + root.resolve("mccape-db") + ";AUTO_SERVER=FALSE");
        try (Statement statement = database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS schema_version(version INT PRIMARY KEY)");
            statement.executeUpdate("MERGE INTO schema_version KEY(version) VALUES(1)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS capes(id VARCHAR(64) PRIMARY KEY, player_uuid VARCHAR(36) NOT NULL, sha256 VARCHAR(64) NOT NULL, file_name VARCHAR(100) NOT NULL, created_at BIGINT NOT NULL)");
            statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS player_cape ON capes(player_uuid)");
        }
    }
    public synchronized StoredCape save(UUID player, Path validatedSource, String sha256) throws Exception {
        String id = UUID.randomUUID().toString(), filename = id + ".png";
        Files.copy(validatedSource, textures.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        try (PreparedStatement delete = database.prepareStatement("DELETE FROM capes WHERE player_uuid=?")) { delete.setString(1, player.toString()); delete.executeUpdate(); }
        try (PreparedStatement insert = database.prepareStatement("INSERT INTO capes VALUES(?,?,?,?,?)")) {
            insert.setString(1, id); insert.setString(2, player.toString()); insert.setString(3, sha256); insert.setString(4, filename); insert.setLong(5, Instant.now().toEpochMilli()); insert.executeUpdate();
        }
        return new StoredCape(id, player, sha256, filename);
    }
    public synchronized Optional<StoredCape> byPlayer(UUID player) throws Exception {
        try (PreparedStatement query = database.prepareStatement("SELECT * FROM capes WHERE player_uuid=?")) {
            query.setString(1, player.toString()); try (ResultSet r = query.executeQuery()) { return r.next() ? Optional.of(read(r)) : Optional.empty(); }
        }
    }
    public synchronized Optional<StoredCape> byId(String id) throws Exception {
        try (PreparedStatement query = database.prepareStatement("SELECT * FROM capes WHERE id=?")) {
            query.setString(1, id); try (ResultSet r = query.executeQuery()) { return r.next() ? Optional.of(read(r)) : Optional.empty(); }
        }
    }
    public synchronized boolean delete(UUID player) throws Exception {
        Optional<StoredCape> old = byPlayer(player);
        try (PreparedStatement delete = database.prepareStatement("DELETE FROM capes WHERE player_uuid=?")) { delete.setString(1, player.toString()); boolean result = delete.executeUpdate() > 0; old.ifPresent(c -> { try { Files.deleteIfExists(textures.resolve(c.fileName())); } catch (Exception ignored) {} }); return result; }
    }
    public Path texture(StoredCape cape) { return textures.resolve(cape.fileName()).normalize(); }
    private StoredCape read(ResultSet r) throws Exception { return new StoredCape(r.getString("id"), UUID.fromString(r.getString("player_uuid")), r.getString("sha256"), r.getString("file_name")); }
    @Override public void close() throws Exception { database.close(); }
    public record StoredCape(String id, UUID player, String sha256, String fileName) {}
}
