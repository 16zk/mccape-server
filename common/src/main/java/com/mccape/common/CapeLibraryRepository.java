package com.mccape.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;

public final class CapeLibraryRepository {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    public CapeLibraryRepository(Path root) { this.file = root.resolve("capes.json"); }
    public CapeLibrary load() throws IOException {
        if (Files.notExists(file)) { CapeLibrary library = new CapeLibrary(); save(library); return library; }
        try {
            CapeLibrary library = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), CapeLibrary.class);
            return library == null ? new CapeLibrary() : library;
        } catch (RuntimeException e) {
            Files.move(file, file.resolveSibling("capes.json.broken-" + Instant.now().toEpochMilli()));
            CapeLibrary library = new CapeLibrary(); save(library); return library;
        }
    }
    public void save(CapeLibrary library) throws IOException { AtomicFileWriter.writeUtf8(file, GSON.toJson(library)); }
}
