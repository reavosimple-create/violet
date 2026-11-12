package com.violet.util;

import net.minecraft.util.math.MathHelper;

public final class Theme {
    public static final int BG = 0xFF08040F, PANEL = 0xFF0E0818, ROW = 0xFF150C24, ROW_HOVER = 0xFF22123B,
            SUB = 0xFF0C0715, ACCENT = 0xFF9119FD, ACCENT_DARK = 0xFF5B0FA8, ACCENT_LIGHT = 0xFFC48BFF,
            TEXT = 0xFFF2EAFF, DIM = 0xFF8C7AA8, BAD = 0xFFFF5C8A;

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
}
