package com.mccape.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PixelCapeDocumentTest {
    @TempDir Path temp;
    @Test void pencilUndoRedo() {
        PixelCapeDocument d = new PixelCapeDocument(); d.set(1, 2, 0xff00ffff);
        assertEquals(0xff00ffff, d.get(1, 2)); assertTrue(d.undo()); assertEquals(0, d.get(1, 2));
        assertTrue(d.redo()); assertEquals(0xff00ffff, d.get(1, 2));
    }
    @Test void fillAndMirror() {
        PixelCapeDocument d = new PixelCapeDocument(); d.set(0, 0, 1); d.fill(1, 0, 2);
        assertEquals(1, d.get(0, 0)); assertEquals(2, d.get(9, 15));
        d.mirrorHorizontal(); assertEquals(1, d.get(9, 0));
    }
    @Test void savesValidCapePng() throws Exception {
        PixelCapeDocument d = new PixelCapeDocument(); Path file = temp.resolve("editor.png"); d.save(file);
        assertEquals(64, new CapeImageValidator().validate(file).width());
    }
}
