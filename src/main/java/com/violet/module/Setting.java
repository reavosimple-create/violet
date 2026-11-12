package com.violet.module;

import com.violet.util.Theme;
import net.minecraft.util.math.MathHelper;

public abstract class Setting {
    public final String name;

    protected Setting(String name) { this.name = name; }

    public static class Bool extends Setting {
        public boolean value;
        public Bool(String n, boolean v) { super(n); value = v; }
    }

    public static class Num extends Setting {
        public double value, min, max, step;
        public Num(String n, double v, double min, double max, double step) {
            super(n); this.value = v; this.min = min; this.max = max; this.step = step;
        }
        public int i() { return (int) Math.round(value); }
        public double fraction() { return (value - min) / (max - min); }
        public void setFromFraction(double f) {
            double raw = min + MathHelper.clamp(f, 0, 1) * (max - min);
            value = MathHelper.clamp(Math.round(raw / step) * step, min, max);
        }
    }

    public static class Mode extends Setting {
        public final String[] modes;
        public int index;
        public Mode(String n, String... modes) { super(n); this.modes = modes; }
        public String get() { return modes[index]; }
        public void cycle(int d) { index = (index + d + modes.length) % modes.length; }
    }

    public static class Hue extends Setting {
        public float hue;
        public boolean rainbow;
        public Hue(String n, float hue) { super(n); this.hue = hue; }
        public int get() { return rainbow ? Theme.rainbow(0) : Theme.hue(hue); }
    }
}
