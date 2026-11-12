package com.violet.hud;

import com.violet.module.Modules;
import com.violet.util.Logo;
import com.violet.util.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class HudRenderer {
    private static long lastNs = System.nanoTime();
    private static float listW, potW, tgtHp = 1f;
    private static LivingEntity lastTarget;
    private static final float[] ka = new float[7];

    private static int accent(int i) {
        return Modules.hudRainbow.value ? Theme.rainbow(i * 18) : Modules.hudColor.get();
    }

    public static void render(DrawContext ctx, RenderTickCounter tc) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        Stats.pollClicks(mc);
        float delta = tc.getTickProgress(false);
        BlockOutlineRenderer.render(ctx, delta);
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        long now = System.currentTimeMillis();
        long ns = System.nanoTime();
        float frame = Math.min(0.1f, (ns - lastNs) / 1e9f);
        lastNs = ns;

        if (Modules.tint.enabled) {
            int a = (int) (Modules.tintStrength.value / 100.0 * 0x70);
            ctx.fill(0, 0, w, h, Theme.alpha(Modules.tintColor.get(), a));
        }

        long dt = now - Stats.hitMs;
        if (Modules.hitFlash.enabled && dt < Modules.flashDur.value) {
            float f = 1f - dt / (float) Modules.flashDur.value;
            int a = (int) (f * Modules.flashStrength.value / 100.0 * 200);
            int c = Modules.flashColor.get() & 0xFFFFFF;
            ctx.fillGradient(0, 0, w, h / 3, (a << 24) | c, c);
            ctx.fillGradient(0, h * 2 / 3, w, h, c, (a << 24) | c);
        }
        if (Modules.hitMarker.enabled && dt < Modules.hitDuration.value) {
            float f = 1f - dt / (float) Modules.hitDuration.value;
            int c = Theme.alpha(Modules.hitColor.get(), Math.max(8, (int) (f * 255)));
            int cx = w / 2, cy = h / 2, g = Modules.crossGap.i() + 3;
            for (int i = 0; i < Modules.hitSize.i(); i++) {
                int o = g + i;
                ctx.fill(cx + o, cy + o, cx + o + 2, cy + o + 2, c);
                ctx.fill(cx - o - 2, cy + o, cx - o, cy + o + 2, c);
                ctx.fill(cx + o, cy - o - 2, cx + o + 2, cy - o, c);
                ctx.fill(cx - o - 2, cy - o - 2, cx - o, cy - o, c);
            }
        }

        if (Modules.lowHealth.enabled && mc.player.isAlive() && mc.player.getHealth() <= Modules.lowHealthAt.value * 2) {
            int a = (int) (70 + 60 * Math.sin(now / 140.0));
            int c = Modules.lowHealthColor.get() & 0xFFFFFF;
            ctx.fillGradient(0, 0, w, h / 4, (a << 24) | c, c);
            ctx.fillGradient(0, h * 3 / 4, w, h, c, (a << 24) | c);
        }
        if (Modules.damageInd.enabled && now - Stats.dmgTime < 1000) {
            float f = (now - Stats.dmgTime) / 1000f;
            int a = Math.max(12, (int) ((1 - f) * 255));
            ctx.drawText(tr, String.format("-%.1f", Stats.dmg), w / 2 + 12, h / 2 - 14 - (int) (20 * f), (a << 24) | 0xFF5C8A, true);
        }

        int y = 4;
        if (Modules.watermark.enabled) {
            Text t = Text.literal("VIOLET").formatted(Formatting.BOLD);
            int nameW = tr.getWidth(t);
            String sub = "client";
            int tw = 26 + nameW + 5 + tr.getWidth(sub) + 8;
            panel(ctx, 4, y, 4 + tw, y + 20);
            Logo.draw(ctx, 9, y + 3, 14);
            ctx.drawText(tr, t, 28, y + 6, 0xFFFFFFFF, true);
            ctx.drawText(tr, sub, 28 + nameW + 5, y + 6, accent(0), true);
            y += 24;
        }

        List<String> l = new ArrayList<>();
        if (Modules.fps.enabled) l.add("FPS  " + mc.getCurrentFps()
                + (Modules.fpsDetails.value ? "  (avg " + Stats.fpsAvg + " / min " + Stats.fpsMin + ")" : ""));
        if (Modules.healthDisplay.enabled) {
            float ab = mc.player.getAbsorptionAmount();
            l.add(String.format("HP  %.1f", mc.player.getHealth()) + (ab > 0 ? String.format(" +%.1f", ab) : ""));
        }
        if (Modules.memory.enabled) {
            Runtime rt = Runtime.getRuntime();
            l.add("Mem  " + (rt.totalMemory() - rt.freeMemory()) / 1048576 + "/" + rt.maxMemory() / 1048576 + " MB");
        }
        if (Modules.light.enabled && mc.world != null) l.add("Light  " + mc.world.getLightLevel(mc.player.getBlockPos()));
        if (Modules.server.enabled) l.add("Server  " + (mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer"));
        if (Modules.players.enabled && mc.getNetworkHandler() != null) l.add("Players  " + mc.getNetworkHandler().getPlayerList().size());
        if (Modules.sprintStatus.enabled) l.add("Sprint  " + (mc.player.isSprinting() ? "ON" : "OFF"));
        if (Modules.cps.enabled) l.add("CPS  " + Stats.cps(0) + " | " + Stats.cps(1));
        if (Modules.ping.enabled) {
            PlayerListEntry e = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) : null;
            l.add("Ping  " + (e != null ? e.getLatency() : 0) + "ms");
        }
        if (Modules.coords.enabled) l.add(String.format("XYZ  %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        if (Modules.direction.enabled) {
            String d = mc.player.getHorizontalFacing().asString();
            l.add("Facing  " + Character.toUpperCase(d.charAt(0)) + d.substring(1));
        }
        if (Modules.speed.enabled) l.add(String.format("Speed  %.2f b/s", Stats.speed));
        if (Modules.clock.enabled) l.add("Time  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        if (Modules.session.enabled) {
            long s = (now - Stats.sessionStart) / 1000;
            l.add(String.format("Session  %d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60));
        }
        if (Modules.combo.enabled && Stats.combo > 0) l.add("Combo  " + Stats.combo);
        if (Modules.reach.enabled && Stats.reach > 0 && now - Stats.lastHit < 5000) l.add(String.format("Reach  %.2f", Stats.reach));
        if (!l.isEmpty()) {
            String[] lab = new String[l.size()], val = new String[l.size()];
            int lw = 0, vw = 0;
            for (int i = 0; i < l.size(); i++) {
                String s = l.get(i);
                int sep = s.indexOf("  ");
                lab[i] = sep < 0 ? s : s.substring(0, sep);
                val[i] = sep < 0 ? "" : s.substring(sep).trim();
                lw = Math.max(lw, tr.getWidth(lab[i]));
                vw = Math.max(vw, tr.getWidth(val[i]));
            }
            float target = 9 + lw + 8 + vw + 8;
            listW = listW <= 0 ? target : Theme.approach(listW, target, frame, 14f);
            int pw = Math.round(listW), ph = l.size() * 12 + 6;
            panel(ctx, 4, y, 4 + pw, y + ph);
            for (int i = 0; i < l.size(); i++) {
                int ry = y + 3 + i * 12;
                Theme.rrect(ctx, 7, ry + 1, 9, ry + 10, 1, accent(i));
                ctx.drawText(tr, lab[i], 13, ry + 2, Theme.DIM, true);
                ctx.drawText(tr, val[i], 13 + lw + 8, ry + 2, Theme.TEXT, true);
            }
            y += ph + 4;
        } else listW = 0;

        if (Modules.potions.enabled && !mc.player.getStatusEffects().isEmpty()) {
            List<String> pl = new ArrayList<>();
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1);
                int secs = e.getDuration() / 20;
                pl.add(name + "  " + (e.isInfinite() ? "inf" : secs / 60 + ":" + String.format("%02d", secs % 60)));
            }
            int mw = 0;
            for (String s : pl) mw = Math.max(mw, tr.getWidth(s));
            float target = mw + 18;
            potW = potW <= 0 ? target : Theme.approach(potW, target, frame, 14f);
            int pw = Math.round(potW), ph = pl.size() * 12 + 6, x = w - 4 - pw;
            panel(ctx, x, 4, w - 4, 4 + ph);
            for (int i = 0; i < pl.size(); i++) {
                int ry = 7 + i * 12;
                Theme.rrect(ctx, w - 9, ry + 1, w - 7, ry + 10, 1, accent(i));
                ctx.drawText(tr, pl.get(i), x + 6, ry + 2, Theme.TEXT, true);
            }
        } else potW = 0;

        if (Modules.armor.enabled) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int ay = h - 110;
            boolean any = false;
            for (EquipmentSlot s : slots) if (!mc.player.getEquippedStack(s).isEmpty()) any = true;
            if (any) panel(ctx, w - 62, ay - 4, w - 4, ay + 4 * 18);
            for (EquipmentSlot s : slots) {
                ItemStack st = mc.player.getEquippedStack(s);
                if (!st.isEmpty()) {
                    ctx.drawItem(st, w - 22, ay);
                    if (st.isDamageable()) {
                        int max = st.getMaxDamage(), pct = 100 * (max - st.getDamage()) / max;
                        String p = pct + "%";
                        ctx.drawText(tr, p, w - 26 - tr.getWidth(p), ay + 4, 0xFF000000 | MathHelper.hsvToRgb(pct / 100f * 0.33f, 0.9f, 1f), true);
                    }
                }
                ay += 18;
            }
        }

        if (Modules.items.enabled) {
            Item[] it = {Items.END_CRYSTAL, Items.TOTEM_OF_UNDYING, Items.GOLDEN_APPLE, Items.ENDER_PEARL, Items.EXPERIENCE_BOTTLE};
            int iy = h / 2 - 45;
            panel(ctx, w - 58, iy - 4, w - 4, iy + it.length * 18 - 2);
            for (Item item : it) {
                int n = 0;
                for (int i = 0; i < mc.player.getInventory().size(); i++) {
                    ItemStack st = mc.player.getInventory().getStack(i);
                    if (st.isOf(item)) n += st.getCount();
                }
                ctx.drawItem(new ItemStack(item), w - 22, iy);
                String s = "x" + n;
                ctx.drawText(tr, s, w - 25 - tr.getWidth(s), iy + 4, n == 0 ? Theme.BAD : Theme.TEXT, true);
                iy += 18;
            }
        }

        if (Modules.keystrokes.enabled) {
            int bx = 6, by = h - 78;
            keyBox(ctx, tr, bx + 20, by, 18, 18, "W", 0, mc.options.forwardKey.isPressed(), frame);
            keyBox(ctx, tr, bx, by + 20, 18, 18, "A", 1, mc.options.leftKey.isPressed(), frame);
            keyBox(ctx, tr, bx + 20, by + 20, 18, 18, "S", 2, mc.options.backKey.isPressed(), frame);
            keyBox(ctx, tr, bx + 40, by + 20, 18, 18, "D", 3, mc.options.rightKey.isPressed(), frame);
            keyBox(ctx, tr, bx, by + 40, 29, 16, "L " + Stats.cps(0), 4, mc.options.attackKey.isPressed(), frame);
            keyBox(ctx, tr, bx + 31, by + 40, 27, 16, "R " + Stats.cps(1), 5, mc.options.useKey.isPressed(), frame);
            keyBox(ctx, tr, bx, by + 58, 58, 6, "", 6, mc.options.jumpKey.isPressed(), frame);
        }

        if (Modules.targetHud.enabled) {
            LivingEntity le = null;
            if (mc.targetedEntity instanceof LivingEntity a) le = a;
            else if (Stats.target instanceof LivingEntity b && b.isAlive() && now - Stats.lastHit < 3000) le = b;
            if (le != null) {
                float f = MathHelper.clamp(le.getHealth() / le.getMaxHealth(), 0f, 1f);
                if (le != lastTarget) { lastTarget = le; tgtHp = f; }
                tgtHp = Theme.approach(tgtHp, f, frame, 9f);
                int pw = 132, px = w / 2 - pw / 2, py = h / 2 + 26;
                panel(ctx, px, py, px + pw, py + 32);
                ctx.drawText(tr, le.getName().getString(), px + 7, py + 6, Theme.TEXT, true);
                String hp = String.format("%.1f", le.getHealth());
                ctx.drawText(tr, hp, px + pw - 7 - tr.getWidth(hp), py + 6, Theme.DIM, true);
                int bx1 = px + 7, bx2 = px + pw - 7;
                Theme.rrect(ctx, bx1, py + 20, bx2, py + 26, 3, 0xFF1A2330);
                int fx = bx1 + (int) ((bx2 - bx1) * tgtHp);
                Theme.rrect(ctx, bx1, py + 20, Math.max(fx, bx1 + 4), py + 26, 3, 0xFF000000 | MathHelper.hsvToRgb(tgtHp * 0.33f, 0.75f, 1f));
            }
        }
    }

    private static void panel(DrawContext ctx, int x1, int y1, int x2, int y2) {
        if (!Modules.hudBg.value) return;
        Theme.shadow(ctx, x1, y1, x2, y2, 4);
        Theme.rrectBorder(ctx, x1, y1, x2, y2, 4, 0xB00A1017, 0x26FFFFFF);
    }

    private static void keyBox(DrawContext ctx, TextRenderer tr, int x, int y, int w, int hgt, String t, int idx, boolean down, float dt) {
        ka[idx] = Theme.approach(ka[idx], down ? 1f : 0f, dt, 22f);
        float a = ka[idx];
        Theme.rrect(ctx, x, y, x + w, y + hgt, 4, Theme.lerp(0xA00A1017, Theme.alpha(accent(0), 0xE6), a));
        if (!t.isEmpty()) ctx.drawText(tr, t, x + (w - tr.getWidth(t)) / 2, y + (hgt - 8) / 2 + 1, Theme.lerp(Theme.TEXT, 0xFF04080C, a), a < 0.5f);
    }
}
