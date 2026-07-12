package com.mccape.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class PixelCapeDocument {
    /** Logical editable back panel. Saving expands it into Minecraft's 64x32 cuboid atlas. */
    public static final int WIDTH = 10, HEIGHT = 16;
    private final int[] pixels = new int[WIDTH * HEIGHT];
    private final PixelEditHistory history = new PixelEditHistory(128);

    public int[] pixels() { return pixels; }
    public int get(int x, int y) { return pixels[index(x, y)]; }
    public void set(int x, int y, int color) {
        int i = index(x, y), before = pixels[i]; if (before == color) return;
        pixels[i] = color; history.record(new PixelEdit(List.of(new PixelChange(i, before, color))));
    }
    public void fill(int x, int y, int color) {
        int target = get(x, y); if (target == color) return;
        boolean[] seen = new boolean[pixels.length]; ArrayDeque<Integer> queue = new ArrayDeque<>();
        ArrayList<PixelChange> changes = new ArrayList<>(); queue.add(index(x, y));
        while (!queue.isEmpty()) {
            int i = queue.removeFirst(); if (seen[i] || pixels[i] != target) continue; seen[i] = true;
            changes.add(new PixelChange(i, target, color)); pixels[i] = color;
            int px = i % WIDTH, py = i / WIDTH;
            if (px > 0) queue.add(i - 1); if (px + 1 < WIDTH) queue.add(i + 1);
            if (py > 0) queue.add(i - WIDTH); if (py + 1 < HEIGHT) queue.add(i + WIDTH);
        }
        history.record(new PixelEdit(changes));
    }
    public void clear() { transform((x, y) -> 0); }
    public void mirrorHorizontal() { transform((x, y) -> get(WIDTH - 1 - x, y)); }
    private void transform(ColorSource source) {
        int[] before = pixels.clone(); ArrayList<PixelChange> changes = new ArrayList<>();
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
            int i = index(x, y), value = source.color(x, y);
            if (before[i] != value) changes.add(new PixelChange(i, before[i], value));
        }
        changes.forEach(c -> pixels[c.index()] = c.after()); history.record(new PixelEdit(changes));
    }
    public boolean undo() { return history.undo(pixels); }
    public boolean redo() { return history.redo(pixels); }
    public void save(Path output) throws IOException {
        BufferedImage image = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
            int value = get(x, y);
            // Both broad faces of the 10x16x1 cape cuboid receive the design.
            image.setRGB(1 + x, 1 + y, value);
            image.setRGB(12 + x, 1 + y, value);
        }
        // Extend edge colors onto the thin side strips to avoid transparent seams.
        for (int y = 0; y < HEIGHT; y++) {
            image.setRGB(0, 1 + y, get(0, y));
            image.setRGB(11, 1 + y, get(WIDTH - 1, y));
            image.setRGB(22, 1 + y, get(0, y));
        }
        if (!ImageIO.write(image, "PNG", output.toFile())) throw new IOException("PNG writer unavailable");
    }
    private int index(int x, int y) {
        if (x < 0 || y < 0 || x >= WIDTH || y >= HEIGHT) throw new IndexOutOfBoundsException();
        return y * WIDTH + x;
    }
    @FunctionalInterface private interface ColorSource { int color(int x, int y); }
}
