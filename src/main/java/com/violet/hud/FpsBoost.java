package com.violet.hud;

import com.violet.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;
import com.violet.module.Module;
import com.violet.module.Setting;

import java.util.ArrayList;
import java.util.List;

/** Applies performance-oriented video options while enabled and restores your originals when disabled/closing. */
public final class FpsBoost {
    private static final List<Runnable> restore = new ArrayList<>();
    private static boolean applied, limited;
    private static String sig = "";
    private static int savedBgFps;

    private static <T> void put(SimpleOption<T> o, T v) {
        T old = o.getValue();
        restore.add(() -> o.setValue(old));
        o.setValue(v);
    }

    private static String signature(Module m) {
        StringBuilder sb = new StringBuilder();
        for (Setting s : m.settings) {
            if (s instanceof Setting.Bool b) sb.append(b.value).append(',');
            else if (s instanceof Setting.Num n) sb.append(n.value).append(',');
            else if (s instanceof Setting.Mode md) sb.append(md.index).append(',');
        }
        return sb.toString();
    }

    public static void restoreAll() {
        for (Runnable r : restore) { try { r.run(); } catch (Exception ignored) {} }
        restore.clear();
        applied = false;
    }

    public static void tick(MinecraftClient mc) {
        GameOptions o = mc.options;
        if (o == null) return;

        boolean want = Modules.fpsBoost.enabled;
        String s = signature(Modules.fpsBoost);
        if (applied && (!want || !s.equals(sig))) restoreAll();
        if (want && !applied) {
            put(o.getViewDistance(), Modules.boostView.i());
            put(o.getSimulationDistance(), Modules.boostSim.i());
            put(o.getEntityDistanceScaling(), Modules.boostEntityDist.value);
            put(o.getParticles(), switch (Modules.boostParticles.get()) {
                case "All" -> ParticlesMode.ALL;
                case "Decreased" -> ParticlesMode.DECREASED;
                default -> ParticlesMode.MINIMAL;
            });
            if (Modules.boostClouds.value) put(o.getCloudRenderMode(), CloudRenderMode.OFF);
            if (Modules.boostShadows.value) put(o.getEntityShadows(), false);
            if (Modules.boostAo.value) put(o.getAo(), false);
            if (Modules.boostBob.value) put(o.getBobView(), false);
            put(o.getBiomeBlendRadius(), Modules.boostBiome.i());
            put(o.getMipmapLevels(), Modules.boostMip.i());
            if (Modules.boostUnlimited.value) put(o.getMaxFps(), 260);
            if (Modules.boostVsync.value) put(o.getEnableVsync(), false);
            sig = s;
            applied = true;
        }

        // Background FPS limiter
        if (Modules.bgLimiter.enabled && !mc.isWindowFocused() && !limited) {
            savedBgFps = o.getMaxFps().getValue();
            o.getMaxFps().setValue(Modules.bgFps.i());
            limited = true;
        } else if (limited && (!Modules.bgLimiter.enabled || mc.isWindowFocused())) {
            o.getMaxFps().setValue(savedBgFps);
            limited = false;
        }
    }
}
