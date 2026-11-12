package com.violet.module;

import com.violet.module.Module.Category;
import java.util.ArrayList;
import java.util.List;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();
    private static final Category V = Category.RENDER, P = Category.PVP, H = Category.HUD, M = Category.MISC, F = Category.PERF;

    private static Module reg(Module m) { ALL.add(m); return m; }
    public static List<Module> in(Category c) { return ALL.stream().filter(m -> m.category == c).toList(); }
    public static void init() {}

    // ---------------- VISUALS ----------------
    public static final Module crosshair = reg(new Module("Custom Crosshair", "Fully custom crosshair", V, true));
    public static final Setting.Mode crossStyle = crosshair.add(new Setting.Mode("Style", "Cross", "T-Shape", "Dot", "Circle", "Square"));
    public static final Setting.Num crossSize = crosshair.add(new Setting.Num("Size", 5, 1, 20, 1));
    public static final Setting.Num crossGap = crosshair.add(new Setting.Num("Gap", 3, 0, 15, 1));
    public static final Setting.Num crossThick = crosshair.add(new Setting.Num("Thickness", 1, 1, 6, 1));
    public static final Setting.Hue crossColor = crosshair.add(new Setting.Hue("Color", 0.78f));
    public static final Setting.Bool crossOutline = crosshair.add(new Setting.Bool("Outline", true));
    public static final Setting.Bool crossDynamic = crosshair.add(new Setting.Bool("Dynamic Gap", false));

    public static final Module hitMarker = reg(new Module("Hit Marker", "Colored X flash when you hit", V, true));
    public static final Setting.Hue hitColor = hitMarker.add(new Setting.Hue("Color", 0.92f));
    public static final Setting.Num hitSize = hitMarker.add(new Setting.Num("Size", 6, 2, 14, 1));
    public static final Setting.Num hitDuration = hitMarker.add(new Setting.Num("Duration (ms)", 350, 100, 1000, 50));

    public static final Module hitFlash = reg(new Module("Hit Flash", "Screen edge flash on hit", V, false));
    public static final Setting.Hue flashColor = hitFlash.add(new Setting.Hue("Color", 0.92f));
    public static final Setting.Num flashStrength = hitFlash.add(new Setting.Num("Strength %", 60, 10, 100, 5));
    public static final Setting.Num flashDur = hitFlash.add(new Setting.Num("Duration (ms)", 250, 100, 800, 50));

    public static final Module tint = reg(new Module("Color Tint", "Full-screen color filter", V, false));
    public static final Setting.Hue tintColor = tint.add(new Setting.Hue("Color", 0.78f));
    public static final Setting.Num tintStrength = tint.add(new Setting.Num("Strength %", 25, 5, 80, 1));

    public static final Module sky = reg(new Module("Custom Sky", "Recolor the sky (may not work on 1.21.11)", V, false));
    public static final Setting.Mode skyMode = sky.add(new Setting.Mode("Preset", "Purple Haze", "Sunset", "Midnight", "Candy", "Toxic", "Custom"));
    public static final Setting.Hue skyHue = sky.add(new Setting.Hue("Custom Color", 0.8f));

    public static final Module zoom = reg(new Module("Zoom", "Hold C to zoom", V, true));
    public static final Setting.Num zoomFov = zoom.add(new Setting.Num("Zoom FOV", 30, 10, 70, 1));

    public static final Module blockOutline = reg(new Module("Block Outline", "Colored, thick outline on the block you look at", V, true));
    public static final Setting.Hue outlineColor = blockOutline.add(new Setting.Hue("Color", 0.78f));
    public static final Setting.Num outlineThick = blockOutline.add(new Setting.Num("Thickness", 2, 1, 8, 1));
    public static final Setting.Bool outlineHideBack = blockOutline.add(new Setting.Bool("Hide Back Edges", true));
    public static final Setting.Num outlineFov = blockOutline.add(new Setting.Num("FOV Correction", 0, -15, 15, 0.5));

    public static final Module viewModel = reg(new Module("View Model", "Move, rotate and resize your hands and held items", V, false));
    public static final Setting.Num vmX = viewModel.add(new Setting.Num("Offset X", 0, -100, 100, 1));
    public static final Setting.Num vmY = viewModel.add(new Setting.Num("Offset Y", 0, -100, 100, 1));
    public static final Setting.Num vmZ = viewModel.add(new Setting.Num("Offset Z (depth)", 0, -100, 100, 1));
    public static final Setting.Num vmScale = viewModel.add(new Setting.Num("Scale %", 100, 10, 200, 5));
    public static final Setting.Num vmRotX = viewModel.add(new Setting.Num("Rotate X", 0, -180, 180, 5));
    public static final Setting.Num vmRotY = viewModel.add(new Setting.Num("Rotate Y", 0, -180, 180, 5));
    public static final Setting.Num vmRotZ = viewModel.add(new Setting.Num("Rotate Z", 0, -180, 180, 5));

    public static final Module lowHealth = reg(new Module("Low Health Warning", "Pulsing screen edges when low on health", V, false));
    public static final Setting.Num lowHealthAt = lowHealth.add(new Setting.Num("Below (hearts)", 5, 1, 10, 1));
    public static final Setting.Hue lowHealthColor = lowHealth.add(new Setting.Hue("Color", 0.97f));

    // ---------------- PVP ----------------
    public static final Module swing = reg(new Module("Swing Speed", "Visual swing duration (lower = faster)", P, false));
    public static final Setting.Num swingTicks = swing.add(new Setting.Num("Ticks", 4, 1, 20, 1));

    public static final Module combo = reg(new Module("Combo Counter", "Hits in a row without being hit", P, true));
    public static final Module reach = reg(new Module("Reach Display", "Distance of your last hit", P, true));
    public static final Module targetHud = reg(new Module("Target HUD", "Health of the entity you target", P, true));
    public static final Module armor = reg(new Module("Armor HUD", "Armor with durability", P, true));
    public static final Module items = reg(new Module("Item Counter", "Crystals, totems, gapples, pearls, XP", P, true));
    public static final Module keystrokes = reg(new Module("Keystrokes", "WASD, mouse and space", P, true));
    public static final Module potions = reg(new Module("Potion Status", "Active effects with timers", P, true));
    public static final Module hitParticles = reg(new Module("Hit Particles", "Extra particles on hit", P, false));
    public static final Setting.Mode particleType = hitParticles.add(new Setting.Mode("Type", "Crit", "Heart", "Enchant", "Flame", "Witch", "End Rod"));
    public static final Setting.Num particleCount = hitParticles.add(new Setting.Num("Amount", 8, 1, 30, 1));

    public static final Module hitSound = reg(new Module("Hit Sound", "Play a sound when you hit something", P, false));
    public static final Setting.Mode hitSoundType = hitSound.add(new Setting.Mode("Sound", "Ding", "Arrow", "Level Up", "Pop", "Glass"));
    public static final Setting.Num hitVolume = hitSound.add(new Setting.Num("Volume", 0.6, 0.1, 1.0, 0.1));
    public static final Setting.Num hitPitch = hitSound.add(new Setting.Num("Pitch", 1.2, 0.5, 2.0, 0.1));
    public static final Module damageInd = reg(new Module("Damage Indicator", "Floating damage number on your target", P, true));

    // ---------------- PERFORMANCE ----------------
    public static final Module fpsBoost = reg(new Module("FPS Boost", "Applies fast video options; restores yours when off", F, false));
    public static final Setting.Num boostView = fpsBoost.add(new Setting.Num("Render Distance", 8, 2, 32, 1));
    public static final Setting.Num boostSim = fpsBoost.add(new Setting.Num("Simulation Distance", 5, 5, 32, 1));
    public static final Setting.Num boostEntityDist = fpsBoost.add(new Setting.Num("Entity Distance", 0.5, 0.5, 5.0, 0.5));
    public static final Setting.Mode boostParticles = fpsBoost.add(new Setting.Mode("Particles", "Minimal", "Decreased", "All"));
    public static final Setting.Bool boostClouds = fpsBoost.add(new Setting.Bool("Clouds Off", true));
    public static final Setting.Bool boostShadows = fpsBoost.add(new Setting.Bool("Entity Shadows Off", true));
    public static final Setting.Bool boostAo = fpsBoost.add(new Setting.Bool("Smooth Lighting Off", true));
    public static final Setting.Bool boostBob = fpsBoost.add(new Setting.Bool("View Bobbing Off", false));
    public static final Setting.Num boostBiome = fpsBoost.add(new Setting.Num("Biome Blend", 0, 0, 7, 1));
    public static final Setting.Num boostMip = fpsBoost.add(new Setting.Num("Mipmap Levels", 0, 0, 4, 1));
    public static final Setting.Bool boostUnlimited = fpsBoost.add(new Setting.Bool("Unlimited FPS", true));
    public static final Setting.Bool boostVsync = fpsBoost.add(new Setting.Bool("VSync Off", true));

    public static final Module bgLimiter = reg(new Module("Background FPS Limiter", "Cap FPS while the window is unfocused", F, true));
    public static final Setting.Num bgFps = bgLimiter.add(new Setting.Num("Limit", 15, 5, 60, 5));

    // ---------------- HUD ----------------
    public static final Module hudStyle = reg(new Module("HUD Style", "Colors for all HUD elements", H, true));
    public static final Setting.Bool hudRainbow = hudStyle.add(new Setting.Bool("Rainbow", false));
    public static final Setting.Hue hudColor = hudStyle.add(new Setting.Hue("Accent", 0.76f));
    public static final Setting.Bool hudBg = hudStyle.add(new Setting.Bool("Background", true));

    public static final Module watermark = reg(new Module("Watermark", "Violet title bar", H, true));
    public static final Module fps = reg(new Module("FPS", "Frames per second", H, true));
    public static final Setting.Bool fpsDetails = fps.add(new Setting.Bool("Show Avg / Min", true));
    public static final Module healthDisplay = reg(new Module("Health", "Your health + absorption", H, false));
    public static final Module memory = reg(new Module("Memory", "RAM used / max", H, false));
    public static final Module light = reg(new Module("Light Level", "Light at your position", H, false));
    public static final Module server = reg(new Module("Server", "Server address", H, false));
    public static final Module players = reg(new Module("Player Count", "Players in the tab list", H, false));
    public static final Module sprintStatus = reg(new Module("Sprint Status", "Shows when you are sprinting", H, false));
    public static final Module cps = reg(new Module("CPS", "Clicks per second (left | right)", H, true));
    public static final Module ping = reg(new Module("Ping", "Server latency", H, true));
    public static final Module coords = reg(new Module("Coordinates", "XYZ position", H, false));
    public static final Module direction = reg(new Module("Direction", "Facing direction", H, false));
    public static final Module speed = reg(new Module("Speedometer", "Blocks per second", H, false));
    public static final Module clock = reg(new Module("Clock", "Real time", H, false));
    public static final Module session = reg(new Module("Session Timer", "Time played this session", H, false));

    // ---------------- MISC ----------------
    public static final Module menuTheme = reg(new Module("Menu Theme", "Purple styling on menus + title button", M, true));
    public static final Module autoRespawn = reg(new Module("Auto Respawn", "Respawn instantly on the death screen", M, false));
    public static final Module toggleSprint = reg(new Module("Toggle Sprint", "Auto-sprint while moving forward", M, false));

    public static int skyColor() {
        return switch (skyMode.get()) {
            case "Sunset" -> 0xFF7A4A;
            case "Midnight" -> 0x0B0B2E;
            case "Candy" -> 0xFF8FD8;
            case "Toxic" -> 0x6BFF3A;
            case "Custom" -> skyHue.get() & 0xFFFFFF;
            default -> 0x8B5CF6;
        };
    }
}
