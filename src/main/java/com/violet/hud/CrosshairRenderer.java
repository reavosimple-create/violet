package com.violet.hud;

import com.violet.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class CrosshairRenderer {
    /** Returns true if the vanilla crosshair should be suppressed. */
    public static boolean render(DrawContext ctx) {
        if (!Modules.crosshair.enabled) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!mc.options.getPerspective().isFirstPerson()) return true;

        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
        int size = Modules.crossSize.i(), gap = Modules.crossGap.i(), t = Modules.crossThick.i();
        if (Modules.crossDynamic.value && Stats.speed > 0.5) gap += Math.min(4, (int) (Stats.speed / 2));
        int col = Modules.crossColor.get();
        boolean ol = Modules.crossOutline.value;
        int half = t / 2;

        switch (Modules.crossStyle.get()) {
            case "Dot" -> { int d = Math.max(1, t); rect(ctx, cx - d, cy - d, cx + d, cy + d, col, ol); }
            case "Circle" -> {
                int r = size + gap;
                for (int i = 0; i < 64; i++) {
                    double a = i * Math.PI * 2 / 64;
                    int x = cx + (int) Math.round(Math.cos(a) * r), y = cy + (int) Math.round(Math.sin(a) * r);
                    ctx.fill(x - half, y - half, x - half + t, y - half + t, col);
                }
            }
            case "Square" -> {
                int r = size + gap;
                rect(ctx, cx - r, cy - r, cx + r, cy - r + t, col, ol);
                rect(ctx, cx - r, cy + r - t, cx + r, cy + r, col, ol);
                rect(ctx, cx - r, cy - r, cx - r + t, cy + r, col, ol);
                rect(ctx, cx + r - t, cy - r, cx + r, cy + r, col, ol);
            }
            default -> {
                boolean tShape = Modules.crossStyle.get().equals("T-Shape");
                rect(ctx, cx - gap - size, cy - half, cx - gap, cy - half + t, col, ol);
                rect(ctx, cx + gap, cy - half, cx + gap + size, cy - half + t, col, ol);
                rect(ctx, cx - half, cy + gap, cx - half + t, cy + gap + size, col, ol);
                if (!tShape) rect(ctx, cx - half, cy - gap - size, cx - half + t, cy - gap, col, ol);
            }
        }
        return true;
    }

    private static void rect(DrawContext ctx, int x1, int y1, int x2, int y2, int col, boolean outline) {
        if (outline) ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        ctx.fill(x1, y1, x2, y2, col);
    }
}
