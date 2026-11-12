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
            int tw = tr.getWidth(t) + 30;
            ctx.fill(4, y, 4 + tw, y + 18, 0xB0000000);
            ctx.fill(4, y, 6, y + 18, Theme.ACCENT);
            Logo.draw(ctx, 9, y + 2, 14);
            ctx.drawText(tr, t, 26, y + 5, 0xFFFFFFFF, true);
            y += 20;
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
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            int bw = tr.getWidth(s) + 8;
            if (Modules.hudBg.value) ctx.fill(4, y, 4 + bw, y + 11, 0x90000000);
            ctx.fill(4, y, 6, y + 11, accent(i));
            ctx.drawText(tr, s, 9, y + 2, Theme.TEXT, true);
            y += 11;
        }

        if (Modules.potions.enabled) {
            int py = 4, i = 0;
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                String name = e.getEffectType().value().getName().getString() + " " + (e.getAmplifier() + 1);
                int secs = e.getDuration() / 20;
                String s = name + "  " + (e.isInfinite() ? "inf" : secs / 60 + ":" + String.format("%02d", secs % 60));
                int bw = tr.getWidth(s) + 8, x = w - bw - 4;
                if (Modules.hudBg.value) ctx.fill(x, py, x + bw, py + 11, 0x90000000);
                ctx.fill(x + bw - 2, py, x + bw, py + 11, accent(i++));
                ctx.drawText(tr, s, x + 3, py + 2, Theme.TEXT, true);
                py += 11;
            }
        }

        if (Modules.armor.enabled) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            int ay = h - 110;
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
            key(ctx, tr, bx + 20, by, 18, "W", mc.options.forwardKey.isPressed());
            key(ctx, tr, bx, by + 20, 18, "A", mc.options.leftKey.isPressed());
            key(ctx, tr, bx + 20, by + 20, 18, "S", mc.options.backKey.isPressed());
            key(ctx, tr, bx + 40, by + 20, 18, "D", mc.options.rightKey.isPressed());
            wide(ctx, tr, bx, by + 40, 29, "L " + Stats.cps(0), mc.options.attackKey.isPressed());
            wide(ctx, tr, bx + 31, by + 40, 27, "R " + Stats.cps(1), mc.options.useKey.isPressed());
            ctx.fill(bx, by + 58, bx + 58, by + 64, mc.options.jumpKey.isPressed() ? Theme.ACCENT : 0x90000000);
        }

        if (Modules.targetHud.enabled) {
            LivingEntity le = null;
            if (mc.targetedEntity instanceof LivingEntity a) le = a;
            else if (Stats.target instanceof LivingEntity b && b.isAlive() && now - Stats.lastHit < 3000) le = b;
            if (le != null) {
                int pw = 120, px = w / 2 - pw / 2, py = h / 2 + 26;
                ctx.fill(px, py, px + pw, py + 26, 0xC0000000);
                ctx.fill(px, py, px + pw, py + 1, accent(0));
                ctx.drawText(tr, le.getName().getString(), px + 4, py + 4, Theme.TEXT, true);
                float f = MathHelper.clamp(le.getHealth() / le.getMaxHealth(), 0f, 1f);
                ctx.fill(px + 4, py + 16, px + pw - 4, py + 20, 0xFF2A1450);
                ctx.fill(px + 4, py + 16, px + 4 + (int) ((pw - 8) * f), py + 20, 0xFF000000 | MathHelper.hsvToRgb(f * 0.33f, 0.8f, 1f));
                String hp = String.format("%.1f", le.getHealth());
                ctx.drawText(tr, hp, px + pw - 4 - tr.getWidth(hp), py + 4, Theme.DIM, true);
            }
        }
    }

    private static void key(DrawContext ctx, TextRenderer tr, int x, int y, int s, String t, boolean down) {
        ctx.fill(x, y, x + s, y + s, down ? Theme.ACCENT : 0x90000000);
        ctx.drawText(tr, t, x + (s - tr.getWidth(t)) / 2, y + (s - 8) / 2, down ? 0xFFFFFFFF : Theme.TEXT, true);
    }

    private static void wide(DrawContext ctx, TextRenderer tr, int x, int y, int w, String t, boolean down) {
        ctx.fill(x, y, x + w, y + 16, down ? Theme.ACCENT : 0x90000000);
        ctx.drawText(tr, t, x + (w - tr.getWidth(t)) / 2, y + 4, down ? 0xFFFFFFFF : Theme.TEXT, true);
    }
}
