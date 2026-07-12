package com.mccape.common;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

public final class CapeLibraryService {
    private final Path root;
    private final CapeLibraryRepository repository;
    private final ConfigManager configManager;

    public CapeLibraryService(Path root) {
        this.root = root;
        this.repository = new CapeLibraryRepository(root);
        this.configManager = new ConfigManager(root);
    }

    public CapeLibrary load() throws IOException { return repository.load(); }

    public void equip(String id) throws IOException {
        CapeLibrary library = load();
        if (library.findById(id) == null) throw new IllegalArgumentException("Unknown cape id");
        McCapeConfig old = configManager.load();
        configManager.save(copyWithSelection(old, id));
    }

    public void unequip() throws IOException {
        McCapeConfig old = configManager.load();
        configManager.save(copyWithSelection(old, null));
    }

    private McCapeConfig copyWithSelection(McCapeConfig old, String selection) {
        return new McCapeConfig(old.schemaVersion(), old.enabled(), selection, old.showOwnCape(),
                old.showRemoteCapes(), old.physicsMode(), old.onlineFeatures(), old.cacheLimitBytes(),
                old.cacheDurationSeconds(), old.logLevel());
    }

    public void rename(String id, String name) throws IOException {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Cape name cannot be blank");
        CapeLibrary library = load();
        CapeDefinition old = required(library, id);
        CapeMetadata metadata = new CapeMetadata(old.metadata().createdAt(), Instant.now().toEpochMilli(),
                old.metadata().width(), old.metadata().height(), old.metadata().sha256());
        library.replace(new CapeDefinition(old.id(), name.strip(), old.relativePath(), old.type(), metadata, old.animation()));
        repository.save(library);
    }

    public CapeDefinition duplicate(String id) throws IOException {
        CapeLibrary library = load();
        CapeDefinition old = required(library, id);
        CapeId newId = CapeId.create();
        String relative = "capes/" + newId.value() + ".png";
        Files.copy(new SafePathResolver().resolve(root, old.relativePath()), new SafePathResolver().resolve(root, relative));
        long now = Instant.now().toEpochMilli();
        CapeDefinition copy = new CapeDefinition(newId, old.name() + " (copy)", relative, old.type(),
                new CapeMetadata(now, now, old.metadata().width(), old.metadata().height(), old.metadata().sha256()), old.animation());
        library.add(copy); repository.save(library); return copy;
    }

    public void delete(String id) throws IOException {
        CapeLibrary library = load();
        CapeDefinition cape = required(library, id);
        Files.deleteIfExists(new SafePathResolver().resolve(root, cape.relativePath()));
        library.remove(id); repository.save(library);
        McCapeConfig config = configManager.load();
        if (id.equals(config.selectedCapeId())) unequip();
    }

    public Path export(String id) throws IOException {
        CapeDefinition cape = required(load(), id);
        Path source = new SafePathResolver().resolve(root, cape.relativePath());
        Path directory = root.resolve("exports");
        String base = cape.name();
        try { return new CapeExporter().export(source, directory, base, false); }
        catch (FileAlreadyExistsException exists) {
            return new CapeExporter().export(source, directory, base + "-" + Instant.now().toEpochMilli(), false);
        }
    }

    private CapeDefinition required(CapeLibrary library, String id) {
        CapeDefinition cape = library.findById(id);
        if (cape == null) throw new IllegalArgumentException("Unknown cape id: " + id);
        return cape;
    }
}
