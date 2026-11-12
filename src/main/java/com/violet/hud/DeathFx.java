package com.violet.hud;

import com.violet.module.Modules;
import com.violet.util.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/** Death effects: a particle burst in the world and a cinematic overlay on the death screen. */
public final class DeathFx {
    private static final Random RNG = new Random();
    private static boolean dead;
    private static long deathAt, aliveSince = System.currentTimeMillis();
    private static double dx, dy, dz;

    private DeathFx() {}

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) { dead = false; aliveSince = System.currentTimeMillis(); return; }
        boolean nowDead = !mc.player.isAlive();
        if (nowDead && !dead) {
            dead = true;
            deathAt = System.currentTimeMillis();
            dx = mc.player.getX(); dy = mc.player.getY(); dz = mc.player.getZ();
            if (Modules.deathFx.enabled && Modules.deathBurst.value) burst(mc);
        } else if (!nowDead && dead) {
            dead = false;
            aliveSince = System.currentTimeMillis();
        }
    }

    private static void burst(MinecraftClient mc) {
        ParticleEffect[] fx = {ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL, ParticleTypes.LARGE_SMOKE};
        int n = Modules.deathBurstCount.i();
        for (int i = 0; i < n; i++) {
            double a = RNG.nextDouble() * Math.PI * 2, sp = 0.05 + RNG.nextDouble() * 0.22;
            mc.particleManager.addParticle(fx[i % fx.length],
                    dx + (RNG.nextDouble() - 0.5) * 0.6, dy + RNG.nextDouble() * 1.8, dz + (RNG.nextDouble() - 0.5) * 0.6,
                    Math.cos(a) * sp, 0.04 + RNG.nextDouble() * 0.16, Math.sin(a) * sp);
        }
    }

    private static float frac(double v) { return (float) (v - Math.floor(v)); }

    /** Called after the vanilla death screen has rendered. */
    public static void render(DrawContext ctx, int w, int h) {
        if (!Modules.deathFx.enabled || !Modules.deathScreenFx.value || deathAt == 0) return;
        float t = (System.currentTimeMillis() - deathAt) / 1000f;
        int c = Modules.deathColor.get() & 0xFFFFFF;
        int cx = w / 2, cy = h / 2;

        // impact flash
        if (t < 0.45f) {
            float f = 1f - t / 0.45f;
            ctx.fill(0, 0, w, h, ((int) (f * f * 150) << 24) | 0xFF6070);
        }

        // pulsing vignette
        float v = Theme.easeOutCubic(Math.min(1f, t / 0.9f));
        int a = (int) (v * (0.85f + 0.15f * (float) Math.sin(t * 2.4f)) * 0xB0);
        ctx.fillGradient(0, 0, w, h / 3, (a << 24) | c, c);
        ctx.fillGradient(0, h * 2 / 3, w, h, c, (a << 24) | c);
        int strip = Math.max(18, w / 14);
        for (int i = 0; i < strip; i++) {
            float k = 1f - i / (float) strip;
            int sa = (int) (a * k * k * 0.8f);
            if (sa < 3) continue;
            ctx.fill(i * 2, 0, i * 2 + 2, h, (sa << 24) | c);
            ctx.fill(w - i * 2 - 2, 0, w - i * 2, h, (sa << 24) | c);
        }

        // shockwave ring
        float rt = Math.min(1f, t / 1.3f);
        if (rt < 1f) {
            float rad = Theme.easeOutCubic(rt) * h * 0.85f;
            int ra = (int) ((1f - rt) * 200);
            for (int i = 0; i < 90; i++) {
                double ang = i * Math.PI * 2 / 90;
                int x = cx + (int) (Math.cos(ang) * rad), y = cy + (int) (Math.sin(ang) * rad);
                ctx.fill(x, y, x + 2, y + 2, (ra << 24) | c);
            }
        }

        // rising embers
        float fade = Math.min(1f, t / 0.7f);
        for (int i = 0; i < 46; i++) {
            float rx = frac(Math.sin(i * 12.9898) * 43758.5453);
            float ry = frac(Math.sin(i * 78.233) * 12345.6789);
            float sp = 24f + 60f * frac(Math.sin(i * 5.1) * 999.1);
            float y = h - ((t * sp + ry * h) % (h + 24f));
            float x = rx * w + (float) Math.sin(t * 1.4f + i) * 9f;
            int ea = (int) (fade * (50 + 150 * (y / h)));
            if (ea < 6) continue;
            int size = i % 5 == 0 ? 3 : 2;
            ctx.fill((int) x, (int) y, (int) x + size, (int) y + size, (Math.min(ea, 255) << 24) | c);
        }
    }

    /** Info pill + title underline, drawn together with the screen overlay. */
    public static void renderInfo(DrawContext ctx, net.minecraft.client.font.TextRenderer tr, int w, int h) {
        if (!Modules.deathFx.enabled || !Modules.deathScreenFx.value || deathAt == 0) return;
        float t = (System.currentTimeMillis() - deathAt) / 1000f;
        int c = 0xFF000000 | (Modules.deathColor.get() & 0xFFFFFF);
        int cx = w / 2;

        float lw = Theme.easeOutCubic(MathHelper.clamp((t - 0.2f) / 0.9f, 0f, 1f)) * 110f;
        ctx.fill(cx - (int) lw, 54, cx + (int) lw, 55, Theme.fade(c, 0.85f));

        float pa = Theme.easeOutCubic(MathHelper.clamp((t - 0.5f) / 0.6f, 0f, 1f));
        if (pa < 0.05f) return;
        long s = Math.max(0, (deathAt - aliveSince) / 1000);
        String txt = String.format("Died at %d, %d, %d    |    Survived %d:%02d", (int) Math.floor(dx), (int) Math.floor(dy), (int) Math.floor(dz), s / 60, s % 60);
        int tw = tr.getWidth(txt), x1 = cx - tw / 2 - 12, y1 = h - 44 + (int) ((1f - pa) * 8f);
        Theme.rrectBorder(ctx, x1, y1, x1 + tw + 24, y1 + 20, 6, Theme.fade(0xE0090D13, pa), Theme.fade(Theme.alpha(c, 0xA0), pa));
        ctx.drawText(tr, txt, x1 + 12, y1 + 6, Theme.fade(0xFFFFFFFF, pa), true);
    }
}
