package com.violet.util;

import com.google.gson.*;
import com.violet.module.*;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("violet.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : Modules.ALL) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.enabled);
                for (Setting s : m.settings) {
                    if (s instanceof Setting.Bool b) o.addProperty(s.name, b.value);
                    else if (s instanceof Setting.Num n) o.addProperty(s.name, n.value);
                    else if (s instanceof Setting.Mode md) o.addProperty(s.name, md.index);
                    else if (s instanceof Setting.Hue h) { o.addProperty(s.name + " hue", h.hue); o.addProperty(s.name + " rainbow", h.rainbow); }
                }
                root.add(m.name, o);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception ignored) {}
    }

    public static void load() {
        try {
            if (!Files.exists(path())) return;
            JsonObject root = JsonParser.parseString(Files.readString(path())).getAsJsonObject();
            for (Module m : Modules.ALL) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
                for (Setting s : m.settings) {
                    if (s instanceof Setting.Bool b && o.has(s.name)) b.value = o.get(s.name).getAsBoolean();
                    else if (s instanceof Setting.Num n && o.has(s.name)) n.value = o.get(s.name).getAsDouble();
                    else if (s instanceof Setting.Mode md && o.has(s.name)) md.index = Math.floorMod(o.get(s.name).getAsInt(), md.modes.length);
                    else if (s instanceof Setting.Hue h) {
                        if (o.has(s.name + " hue")) h.hue = o.get(s.name + " hue").getAsFloat();
                        if (o.has(s.name + " rainbow")) h.rainbow = o.get(s.name + " rainbow").getAsBoolean();
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
