package com.mccape.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class CapeImageTest {
    @TempDir Path temp;

    private Path png(String name, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x80ff0000);
        Path path = temp.resolve(name);
        ImageIO.write(image, "PNG", path.toFile());
        return path;
    }

    @Test void validatesRealPngAndAlpha() throws Exception {
        CapeImageInfo info = new CapeImageValidator().validate(png("cape.png", 64, 32));
        assertEquals(64, info.width()); assertTrue(info.hasAlpha()); assertEquals(64, info.sha256().length());
    }

    @Test void rejectsFakePngAndDimensions() throws Exception {
        Path fake = temp.resolve("fake.png"); Files.writeString(fake, "not a png");
        assertThrows(McCapeException.class, () -> new CapeImageValidator().validate(fake));
        assertThrows(McCapeException.class, () -> new CapeImageValidator().validate(png("square.png", 64, 64)));
    }

    @Test void importsOnceAndDetectsDuplicate() throws Exception {
        Path root = temp.resolve("mccape");
        Path source = png("source.png", 64, 32);
        CapeImporter importer = new CapeImporter(root);
        CapeImportResult first = importer.importPng(source, "My Cape");
        CapeImportResult second = importer.importPng(source, "Copy");
        assertFalse(first.duplicate()); assertTrue(second.duplicate());
        assertTrue(Files.exists(root.resolve(first.cape().relativePath())));
        assertEquals(1, new CapeLibraryRepository(root).load().getCapes().size());
    }

    @Test void exportsWithoutSilentOverwrite() throws Exception {
        Path source = png("source.png", 64, 32);
        CapeExporter exporter = new CapeExporter();
        Path output = exporter.export(source, temp.resolve("exports"), "unsafe/name", false);
        assertTrue(Files.exists(output));
        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(source, output.getParent(), "unsafe/name", false));
    }

    @Test void libraryActionsKeepFilesAndSelectionConsistent() throws Exception {
        Path root = temp.resolve("library");
        CapeDefinition cape = new CapeImporter(root).importPng(png("actions.png", 64, 32), "Original").cape();
        CapeLibraryService service = new CapeLibraryService(root);
        service.equip(cape.id().value());
        assertEquals(cape.id().value(), new ConfigManager(root).load().selectedCapeId());
        service.rename(cape.id().value(), "Renamed");
        assertEquals("Renamed", service.load().findById(cape.id().value()).name());
        CapeDefinition copy = service.duplicate(cape.id().value());
        assertTrue(Files.exists(root.resolve(copy.relativePath())));
        assertTrue(Files.exists(service.export(cape.id().value())));
        service.delete(cape.id().value());
        assertNull(new ConfigManager(root).load().selectedCapeId());
        assertNull(service.load().findById(cape.id().value()));
    }
}
