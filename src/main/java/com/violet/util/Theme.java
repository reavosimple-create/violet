package com.violet.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

public final class Theme {
    // "Glacier" palette: deep blue-black surfaces with an electric cyan accent.
    public static final int BG = 0xFF090D13, PANEL = 0xFF0D131B, ROW = 0xFF131B26, ROW_HOVER = 0xFF1B2636,
            SUB = 0xFF0F1620, ACCENT = 0xFF22D3EE, ACCENT_DARK = 0xFF0E7490, ACCENT_LIGHT = 0xFF8BEAFB,
            TEXT = 0xFFEAF6FA, DIM = 0xFF7C8DA0, BAD = 0xFFFF5C7A, OFF = 0xFF263244;

    private Theme() {}

    public static int rainbow(double offset) {
        float h = (float) (((System.currentTimeMillis() / 20.0) + offset) % 360.0) / 360f;
        return 0xFF000000 | MathHelper.hsvToRgb(h, 0.65f, 1f);
    }

    public static int hue(float h) {
        return 0xFF000000 | MathHelper.hsvToRgb(h, 0.75f, 1f);
    }

    public static int alpha(int rgb, int a) {
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    /** Multiplies the alpha channel of an ARGB color by f (0..1). */
    public static int fade(int argb, float f) {
        int a = (int) (((argb >>> 24) & 0xFF) * MathHelper.clamp(f, 0f, 1f));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = MathHelper.clamp(t, 0f, 1f);
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int) (aa + (ba - aa) * t) << 24) | ((int) (ar + (br - ar) * t) << 16)
                | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    public static float easeOutCubic(float t) {
        t = MathHelper.clamp(t, 0f, 1f);
        float u = 1f - t;
        return 1f - u * u * u;
    }

    public static float easeOutBack(float t) {
        t = MathHelper.clamp(t, 0f, 1f);
        float c1 = 1.35f, c3 = c1 + 1f, u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    /** Frame-rate independent smoothing: moves cur toward target. */
    public static float approach(float cur, float target, float dt, float speed) {
        return cur + (target - cur) * MathHelper.clamp(dt * speed, 0f, 1f);
    }

    /** Filled rounded rectangle (corner radius r, in pixels). Safe with translucent colors. */
    public static void rrect(DrawContext ctx, int x1, int y1, int x2, int y2, int r, int color) {
        if (x2 <= x1 || y2 <= y1) return;
        r = Math.max(0, Math.min(r, Math.min((x2 - x1) / 2, (y2 - y1) / 2)));
        if (r == 0) { ctx.fill(x1, y1, x2, y2, color); return; }
        ctx.fill(x1, y1 + r, x2, y2 - r, color);
        for (int i = 0; i < r; i++) {
            float dy = r - i - 0.5f;
            int inset = (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
            ctx.fill(x1 + inset, y1 + i, x2 - inset, y1 + i + 1, color);
            ctx.fill(x1 + inset, y2 - i - 1, x2 - inset, y2 - i, color);
        }
    }

    /** Rounded rectangle with a 1px rounded border drawn behind it. */
    public static void rrectBorder(DrawContext ctx, int x1, int y1, int x2, int y2, int r, int fill, int border) {
        rrect(ctx, x1 - 1, y1 - 1, x2 + 1, y2 + 1, r + 1, border);
        rrect(ctx, x1, y1, x2, y2, r, fill);
    }

    /** Soft drop shadow made of a few stacked translucent rounded rectangles. */
    public static void shadow(DrawContext ctx, int x1, int y1, int x2, int y2, int r) {
        for (int i = 4; i >= 1; i--) rrect(ctx, x1 - i, y1 - i + 2, x2 + i, y2 + i + 2, r + i, 0x0C000000);
    }
}
