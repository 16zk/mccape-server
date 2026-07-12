package com.mccape.common;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

public final class BackupManager {
    private final Path directory;
    public BackupManager(Path directory) { this.directory = directory; }
    public Path backup(Path source) throws IOException {
        Files.createDirectories(directory);
        Path result = directory.resolve(source.getFileName() + "." + Instant.now().toEpochMilli() + ".bak");
        return Files.copy(source, result, StandardCopyOption.REPLACE_EXISTING);
    }
}
