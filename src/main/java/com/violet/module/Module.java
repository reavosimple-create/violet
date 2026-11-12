package com.violet.module;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public enum Category {
        RENDER("Visuals"), PVP("PvP"), HUD("HUD"), PERF("Performance"), MISC("Misc");
        public final String label;
        Category(String l) { label = l; }
    }

    public final String name, desc;
    public final Category category;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();

    public Module(String name, String desc, Category c, boolean on) {
        this.name = name; this.desc = desc; this.category = c; this.enabled = on;
    }

    public <T extends Setting> T add(T s) { settings.add(s); return s; }
    public void toggle() { enabled = !enabled; }
}
