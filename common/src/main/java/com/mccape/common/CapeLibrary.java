package com.mccape.common;

import java.util.ArrayList;
import java.util.List;

public final class CapeLibrary {
    private int schemaVersion = 1;
    private List<CapeDefinition> capes = new ArrayList<>();

    public int getSchemaVersion() { return schemaVersion; }
    public List<CapeDefinition> getCapes() { return List.copyOf(capes); }
    public void add(CapeDefinition cape) {
        if (capes.stream().anyMatch(existing -> existing.id().equals(cape.id())))
            throw new IllegalArgumentException("Duplicate cape id: " + cape.id().value());
        capes.add(cape);
    }
    public CapeDefinition findByHash(String hash) {
        return capes.stream().filter(c -> c.metadata().sha256().equals(hash)).findFirst().orElse(null);
    }
    public CapeDefinition findById(String id) {
        return capes.stream().filter(c -> c.id().value().equals(id)).findFirst().orElse(null);
    }
    public void remove(String id) { capes.removeIf(c -> c.id().value().equals(id)); }
    public void replace(CapeDefinition cape) {
        for (int i = 0; i < capes.size(); i++) {
            if (capes.get(i).id().equals(cape.id())) { capes.set(i, cape); return; }
        }
        throw new IllegalArgumentException("Unknown cape id: " + cape.id().value());
    }
}
