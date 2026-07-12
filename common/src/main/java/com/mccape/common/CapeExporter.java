package com.mccape.common;

import java.io.IOException;
import java.nio.file.*;

public final class CapeExporter {
    public Path export(Path source, Path directory, String name, boolean overwrite) throws IOException {
        Files.createDirectories(directory);
        String safe = name.replaceAll("[^a-zA-Z0-9._ -]", "_").strip();
        if (safe.isBlank()) safe = "cape";
        Path output = directory.resolve(safe + (safe.toLowerCase().endsWith(".png") ? "" : ".png"));
        return Files.copy(source, output, overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[]{});
    }
}
