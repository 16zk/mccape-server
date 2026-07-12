package com.mccape.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

/** Stores both cape metadata and PNG bytes in the database. */
public final class CapeStore implements AutoCloseable {
    private final Connection database;

    public CapeStore(Path localRoot, String databaseUrl) throws Exception {
        boolean postgres = databaseUrl != null && !databaseUrl.isBlank();
        if (postgres) {
            database = DriverManager.getConnection(normalizePostgresUrl(databaseUrl));
        } else {
            Files.createDirectories(localRoot);
            database = DriverManager.getConnection(
                    "jdbc:h2:file:" + localRoot.resolve("mccape-db") + ";AUTO_SERVER=FALSE");
        }

        String binaryType = postgres ? "BYTEA" : "BLOB";
        try (Statement statement = database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS capes(" +
                    "id VARCHAR(64) PRIMARY KEY, " +
                    "player_uuid VARCHAR(36) NOT NULL UNIQUE, " +
                    "sha256 VARCHAR(64) NOT NULL, " +
                    "texture_data " + binaryType + " NOT NULL, " +
                    "created_at BIGINT NOT NULL)");
        }
    }

    public synchronized StoredCape save(UUID player, Path validatedSource, String sha256) throws Exception {
        String id = UUID.randomUUID().toString();
        byte[] png = Files.readAllBytes(validatedSource);

        boolean oldAutoCommit = database.getAutoCommit();
        database.setAutoCommit(false);
        try {
            try (PreparedStatement delete = database.prepareStatement(
                    "DELETE FROM capes WHERE player_uuid=?")) {
                delete.setString(1, player.toString());
                delete.executeUpdate();
            }
            try (PreparedStatement insert = database.prepareStatement(
                    "INSERT INTO capes(id, player_uuid, sha256, texture_data, created_at) VALUES(?,?,?,?,?)")) {
                insert.setString(1, id);
                insert.setString(2, player.toString());
                insert.setString(3, sha256);
                insert.setBytes(4, png);
                insert.setLong(5, Instant.now().toEpochMilli());
                insert.executeUpdate();
            }
            database.commit();
        } catch (Exception e) {
            database.rollback();
            throw e;
        } finally {
            database.setAutoCommit(oldAutoCommit);
        }
        return new StoredCape(id, player, sha256, png);
    }

    public synchronized Optional<StoredCape> byPlayer(UUID player) throws Exception {
        try (PreparedStatement query = database.prepareStatement(
                "SELECT id, player_uuid, sha256, texture_data FROM capes WHERE player_uuid=?")) {
            query.setString(1, player.toString());
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? Optional.of(read(result)) : Optional.empty();
            }
        }
    }

    public synchronized Optional<StoredCape> byId(String id) throws Exception {
        try (PreparedStatement query = database.prepareStatement(
                "SELECT id, player_uuid, sha256, texture_data FROM capes WHERE id=?")) {
            query.setString(1, id);
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? Optional.of(read(result)) : Optional.empty();
            }
        }
    }

    public synchronized boolean delete(UUID player) throws Exception {
        try (PreparedStatement delete = database.prepareStatement(
                "DELETE FROM capes WHERE player_uuid=?")) {
            delete.setString(1, player.toString());
            return delete.executeUpdate() > 0;
        }
    }

    private StoredCape read(ResultSet result) throws Exception {
        return new StoredCape(
                result.getString("id"),
                UUID.fromString(result.getString("player_uuid")),
                result.getString("sha256"),
                result.getBytes("texture_data"));
    }

    private static String normalizePostgresUrl(String value) {
        if (value.startsWith("jdbc:")) return value;
        if (value.startsWith("postgres://")) return "jdbc:postgresql://" + value.substring("postgres://".length());
        if (value.startsWith("postgresql://")) return "jdbc:" + value;
        throw new IllegalArgumentException("DATABASE_URL must be a PostgreSQL URL");
    }

    @Override
    public void close() throws Exception {
        database.close();
    }

    public record StoredCape(String id, UUID player, String sha256, byte[] texture) {}
}
