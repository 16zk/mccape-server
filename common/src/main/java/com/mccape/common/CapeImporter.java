package com.mccape.common;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

public final class CapeImporter {
    private final Path root;
    private final CapeLibraryRepository repository;
    public CapeImporter(Path root) { this.root = root; this.repository = new CapeLibraryRepository(root); }
    public CapeImportResult importPng(Path source, String name) throws IOException, McCapeException {
        if (name == null || name.isBlank()) throw new McCapeException("Cape name cannot be blank");
        CapeImportInspection inspection = new CapeFileInspector().inspect(source);
        CapeImageInfo info = inspection.image();
        CapeLibrary library = repository.load();
        CapeDefinition duplicate = library.findByHash(info.sha256());
        if (duplicate != null) return new CapeImportResult(duplicate, true);
        CapeId id = CapeId.create();
        String relative = "capes/" + id.value() + ".png";
        Path destination = new SafePathResolver().resolve(root, relative);
        Files.createDirectories(destination.getParent());
        Files.copy(source, destination, StandardCopyOption.COPY_ATTRIBUTES);
        long now = Instant.now().toEpochMilli();
        CapeDefinition cape = new CapeDefinition(id, name.strip(), relative,
                inspection.animated() ? CapeType.ANIMATED : CapeType.STATIC,
                new CapeMetadata(now, now, info.width(), info.height(), info.sha256()), inspection.animation());
        library.add(cape); repository.save(library);
        return new CapeImportResult(cape, false);
    }
}
