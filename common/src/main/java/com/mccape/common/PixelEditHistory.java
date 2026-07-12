package com.mccape.common;

import java.util.ArrayDeque;
import java.util.Deque;

public final class PixelEditHistory {
    private final int limit;
    private final Deque<PixelEdit> undo = new ArrayDeque<>(), redo = new ArrayDeque<>();
    public PixelEditHistory(int limit) { if (limit < 1) throw new IllegalArgumentException(); this.limit = limit; }
    public void record(PixelEdit edit) {
        if (edit.changes().isEmpty()) return;
        undo.addLast(edit); redo.clear(); while (undo.size() > limit) undo.removeFirst();
    }
    public boolean undo(int[] pixels) { if (undo.isEmpty()) return false; PixelEdit e = undo.removeLast(); e.changes().forEach(c -> pixels[c.index()] = c.before()); redo.addLast(e); return true; }
    public boolean redo(int[] pixels) { if (redo.isEmpty()) return false; PixelEdit e = redo.removeLast(); e.changes().forEach(c -> pixels[c.index()] = c.after()); undo.addLast(e); return true; }
    public boolean canUndo() { return !undo.isEmpty(); }
    public boolean canRedo() { return !redo.isEmpty(); }
}
