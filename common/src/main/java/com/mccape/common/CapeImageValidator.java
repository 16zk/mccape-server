package com.mccape.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public final class CapeImageValidator {
    private static final byte[] PNG = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
    private final long maxBytes;
    private final int maxPixels;
    public CapeImageValidator() { this(5L * 1024 * 1024, 16_777_216); }
    public CapeImageValidator(long maxBytes, int maxPixels) { this.maxBytes = maxBytes; this.maxPixels = maxPixels; }
    public CapeImageInfo validate(Path path) throws McCapeException {
        try {
            long size = Files.size(path);
            if (size <= 8 || size > maxBytes) throw new McCapeException("PNG file size is invalid");
            try (InputStream input = Files.newInputStream(path)) {
                if (!Arrays.equals(input.readNBytes(8), PNG)) throw new McCapeException("File is not a real PNG");
            }
            BufferedImage image = ImageIO.read(path.toFile());
            if (image == null) throw new McCapeException("PNG is truncated or unsupported");
            int width = image.getWidth(), height = image.getHeight();
            if ((long) width * height > maxPixels || width < 64 || height < 32 || width != height * 2 || width % 64 != 0)
                throw new McCapeException("Cape dimensions must be 64x32 or an integer multiple");
            return new CapeImageInfo(width, height, image.getColorModel().hasAlpha(), size, new ImageHashService().sha256(path));
        } catch (IOException e) { throw new McCapeException("Unable to read PNG", e); }
    }
}
