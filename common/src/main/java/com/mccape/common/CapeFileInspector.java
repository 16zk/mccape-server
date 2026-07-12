package com.mccape.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;

public final class CapeFileInspector {
    private static final byte[] PNG = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    public CapeImportInspection inspect(Path path) throws McCapeException {
        try {
            long size = Files.size(path);
            if (size <= 8 || size > 5L * 1024 * 1024) throw new McCapeException("PNG file size is invalid");
            try (InputStream input = Files.newInputStream(path)) {
                if (!Arrays.equals(input.readNBytes(8), PNG)) throw new McCapeException("File is not a real PNG");
            }
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) throw new McCapeException("PNG is truncated or unsupported");
            int width = image.getWidth(), totalHeight = image.getHeight(), frameHeight = width / 2;
            if (width < 64 || width % 64 != 0 || frameHeight < 32 || totalHeight % frameHeight != 0)
                throw new McCapeException("Expected a 2:1 cape or a vertical cape spritesheet");
            int frames = totalHeight / frameHeight;
            if (frames < 1 || frames > 32 || (long) width * totalHeight > 16_777_216)
                throw new McCapeException("Animated cape frame count or resolution is too large");
            String hash = new ImageHashService().sha256(path);
            CapeImageInfo info = new CapeImageInfo(width, frameHeight, image.getColorModel().hasAlpha(), size, hash);
            CapeAnimation animation = frames > 1 ? new CapeAnimation(width, frameHeight, frames, 120, true) : null;
            return new CapeImportInspection(info, animation);
        } catch (McCapeException e) { throw e; }
        catch (Exception e) { throw new McCapeException("Unable to inspect PNG", e); }
    }
}
