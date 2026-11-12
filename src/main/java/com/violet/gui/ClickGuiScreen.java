package com.violet.gui;

import com.violet.module.Module;
import com.violet.module.Modules;
import com.violet.module.Setting;
import com.violet.util.Config;
import com.violet.util.Logo;
import com.violet.util.Theme;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClickGuiScreen extends Screen {
    private interface Act { void run(int button, double mx); }
    private record Region(int x1, int y1, int x2, int y2, Act act, boolean drag) {}

    private static final int W = 430, H = 268, SIDE = 104;
    private final List<Region> regions = new ArrayList<>();
    private final Set<Module> open = new HashSet<>();
    private final Map<Object, Float> anim = new HashMap<>();
    private Module.Category tab = Module.Category.RENDER;
    private Act dragging;
    private double scroll, maxScroll;
    private float scrollV, tabY = -1f, lastScale = 1f;
    private final long openAt = System.nanoTime();
    private long lastFrame = openAt;
    private String hoverDesc = "";

    public ClickGuiScreen() { super(Text.literal("Violet")); }

    @Override public boolean shouldPause() { return false; }
    @Override public void removed() { Config.save(); }

    private static boolean in(double mx, double my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    private void add(int x1, int y1, int x2, int y2, Act a, boolean drag, int top, int bot) {
        int s = Math.max(y1, top), e = Math.min(y2, bot);
        if (s < e) regions.add(new Region(x1, s, x2, e, a, drag));
    }

    private float an(Object key, float target, float dt) {
        float cur = anim.getOrDefault(key, target);
        cur = Theme.approach(cur, target, dt, 16f);
        anim.put(key, cur);
        return cur;
    }

    private float openProgress() { return Math.min(1f, (System.nanoTime() - openAt) / 280e6f); }

    private double[] toLocal(double mx, double my) {
        float p = openProgress();
        if (p >= 1f) return new double[]{mx, my};
        float s = 0.9f + 0.1f * Theme.easeOutBack(p);
        double cx = width / 2.0, cy = height / 2.0 + (1f - p) * 10f;
        double ocy = height / 2.0;
        return new double[]{cx + (mx - cx) / s, ocy + (my - cy) / s};
    }

    @Override
    public void render(DrawContext ctx, int rawMx, int rawMy, float delta) {
        regions.clear();
        hoverDesc = "";
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1e9f);
        lastFrame = now;
        float p = openProgress();
        boolean animating = p < 1f;
        float e = Theme.easeOutBack(p);
        float s = 0.9f + 0.1f * e;
        float pcx = width / 2f, pcy = height / 2f;

        double[] loc = toLocal(rawMx, rawMy);
        int mx = (int) loc[0], my = (int) loc[1];

        long handle = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (dragging != null) { if (down) dragging.run(0, mx); else dragging = null; }

        // backdrop
        ctx.fillGradient(0, 0, width, height, Theme.fade(0xD0050910, p), Theme.fade(0xE5040810, p));
        ctx.fillGradient(0, height * 2 / 3, width, height, 0x00000000, Theme.fade(Theme.alpha(Theme.ACCENT, 0x2A), p));

        int x0 = (width - W) / 2, y0 = (height - H) / 2;
        if (animating) {
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(pcx, pcy + (1f - p) * 10f);
            ctx.getMatrices().scale(s, s);
            ctx.getMatrices().translate(-pcx, -pcy);
        }

        Theme.shadow(ctx, x0, y0, x0 + W, y0 + H, 7);
        Theme.rrectBorder(ctx, x0, y0, x0 + W, y0 + H, 7, Theme.PANEL, Theme.alpha(Theme.ACCENT, 0x60));

        // header
        Logo.draw(ctx, x0 + 10, y0 + 6, 18);
        ctx.drawText(textRenderer, Text.literal("VIOLET").formatted(Formatting.BOLD), x0 + 34, y0 + 7, 0xFFFFFFFF, true);
        ctx.drawText(textRenderer, "client", x0 + 34 + textRenderer.getWidth(Text.literal("VIOLET").formatted(Formatting.BOLD)) + 5, y0 + 7, Theme.ACCENT, true);
        String hint = "RSHIFT";
        int hw = textRenderer.getWidth(hint) + 12;
        Theme.rrect(ctx, x0 + W - hw - 10, y0 + 7, x0 + W - 10, y0 + 21, 4, Theme.ROW);
        ctx.drawText(textRenderer, hint, x0 + W - hw - 4, y0 + 10, Theme.DIM, false);
        ctx.fill(x0 + 10, y0 + 29, x0 + W - 10, y0 + 30, Theme.alpha(Theme.ACCENT, 0x28));

        // sidebar card
        int sx1 = x0 + 8, sx2 = x0 + SIDE - 4, sy1 = y0 + 36, sy2 = y0 + H - 8;
        Theme.rrect(ctx, sx1, sy1, sx2, sy2, 6, Theme.SUB);
        Module.Category[] cats = Module.Category.values();
        float targetTabY = sy1 + 8 + tab.ordinal() * 28;
        tabY = tabY < 0 ? targetTabY : Theme.approach(tabY, targetTabY, dt, 18f);
        Theme.rrect(ctx, sx1 + 5, (int) tabY, sx2 - 5, (int) tabY + 24, 5, Theme.alpha(Theme.ACCENT, 0x2E));
        ctx.fill(sx1 + 5, (int) tabY + 5, sx1 + 7, (int) tabY + 19, Theme.ACCENT);
        int ty = sy1 + 8;
        for (Module.Category c : cats) {
            boolean sel = c == tab, hov = in(mx, my, sx1 + 5, ty, sx2 - 5, ty + 24);
            float h = an("tab#" + c.name(), hov || sel ? 1f : 0f, dt);
            if (!sel && h > 0.01f) Theme.rrect(ctx, sx1 + 5, ty, sx2 - 5, ty + 24, 5, Theme.fade(Theme.ROW_HOVER, h));
            ctx.drawText(textRenderer, c.label, sx1 + 14, ty + 8, Theme.lerp(Theme.DIM, 0xFFFFFFFF, h), true);
            long onCount = Modules.in(c).stream().filter(m -> m.enabled).count();
            String cnt = String.valueOf(onCount);
            ctx.drawText(textRenderer, cnt, sx2 - 11 - textRenderer.getWidth(cnt), ty + 8, sel ? Theme.ACCENT_LIGHT : Theme.OFF, false);
            final Module.Category cc = c;
            regions.add(new Region(sx1 + 5, ty, sx2 - 5, ty + 24, (b, x) -> { tab = cc; scroll = 0; scrollV = 0; }, false));
            ty += 28;
        }

        // content
        int cx1 = x0 + SIDE + 6, cx2 = x0 + W - 12, top = y0 + 38, bot = y0 + H - 26;
        scrollV = Theme.approach(scrollV, (float) scroll, dt, 18f);
        ctx.enableScissor(cx1 - 3, top, cx2 + 3, bot);
        int y = top - (int) scrollV;
        for (Module m : Modules.in(tab)) y = module(ctx, m, cx1, cx2, y, mx, my, top, bot, dt);
        ctx.disableScissor();
        maxScroll = Math.max(0, (y + (int) scrollV) - bot);
        scroll = MathHelper.clamp(scroll, 0, maxScroll);

        // footer
        ctx.fill(cx1, y0 + H - 22, cx2, y0 + H - 21, Theme.alpha(Theme.ACCENT, 0x20));
        long on = Modules.ALL.stream().filter(m -> m.enabled).count();
        ctx.drawText(textRenderer, hoverDesc.isEmpty() ? on + " enabled   left click: toggle   right click: settings" : hoverDesc,
                cx1 + 2, y0 + H - 15, hoverDesc.isEmpty() ? Theme.OFF : Theme.DIM, false);

        if (animating) {
            Theme.rrect(ctx, x0 - 1, y0 - 1, x0 + W + 1, y0 + H + 1, 8, Theme.alpha(0x04070C, (int) ((1 - p) * (1 - p) * 255)));
            ctx.getMatrices().popMatrix();
        }
    }

    private int module(DrawContext ctx, Module m, int x1, int x2, int y, int mx, int my, int top, int bot, float dt) {
        int h = 24;
        boolean hov = in(mx, my, x1, y, x2, y + h) && my >= top && my < bot;
        if (hov) hoverDesc = m.desc;
        float t = an(m, m.enabled ? 1f : 0f, dt);
        float hv = an(m.name + "#h", hov ? 1f : 0f, dt);
        Theme.rrect(ctx, x1, y, x2, y + h, 5, Theme.lerp(Theme.ROW, Theme.ROW_HOVER, hv));
        if (t > 0.02f) Theme.rrect(ctx, x1, y + 5, x1 + 2, y + h - 5, 1, Theme.fade(Theme.ACCENT, t));
        ctx.drawText(textRenderer, m.name, x1 + 10, y + 8, Theme.lerp(0xFF9AA9BA, 0xFFFFFFFF, Math.max(t, hv * 0.6f)), true);
        if (!m.settings.isEmpty()) ctx.drawText(textRenderer, open.contains(m) ? "v" : ">", x2 - 50, y + 8, Theme.DIM, false);
        toggle(ctx, x2 - 36, y + 6, 26, 12, t);
        add(x1, y, x2, y + h, (b, x) -> {
            if (b == 0) m.toggle();
            else if (b == 1 && !m.settings.isEmpty()) { if (!open.remove(m)) open.add(m); }
        }, false, top, bot);
        y += h + 3;
        if (open.contains(m)) for (Setting s : m.settings) y = setting(ctx, s, x1 + 8, x2, y, mx, my, top, bot, dt);
        return y;
    }

    private void toggle(DrawContext ctx, int x, int y, int w, int h, float t) {
        Theme.rrect(ctx, x, y, x + w, y + h, h / 2, Theme.lerp(Theme.OFF, Theme.ACCENT_DARK, t));
        if (t > 0.02f) Theme.rrect(ctx, x, y, x + w, y + h, h / 2, Theme.fade(Theme.alpha(Theme.ACCENT, 0x70), t));
        int k = h - 4;
        int kx = x + 2 + (int) ((w - k - 4) * t);
        Theme.rrect(ctx, kx, y + 2, kx + k, y + 2 + k, k / 2, Theme.lerp(0xFF8A9AAD, 0xFFFFFFFF, t));
    }

    private int setting(DrawContext ctx, Setting s, int x1, int x2, int y, int mx, int my, int top, int bot, float dt) {
        if (s instanceof Setting.Bool b) {
            Theme.rrect(ctx, x1, y, x2, y + 18, 4, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 5, Theme.TEXT, true);
            toggle(ctx, x2 - 30, y + 4, 22, 10, an(b, b.value ? 1f : 0f, dt));
            add(x1, y, x2, y + 18, (btn, x) -> b.value = !b.value, false, top, bot);
            return y + 20;
        }
        if (s instanceof Setting.Mode md) {
            Theme.rrect(ctx, x1, y, x2, y + 18, 4, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 5, Theme.TEXT, true);
            String v = "< " + md.get() + " >";
            ctx.drawText(textRenderer, v, x2 - 8 - textRenderer.getWidth(v), y + 5, Theme.ACCENT_LIGHT, true);
            add(x1, y, x2, y + 18, (btn, x) -> md.cycle(btn == 1 ? -1 : 1), false, top, bot);
            return y + 20;
        }
        if (s instanceof Setting.Num n) {
            Theme.rrect(ctx, x1, y, x2, y + 26, 4, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 4, Theme.TEXT, true);
            String v = n.step >= 1 ? String.valueOf(n.i()) : String.format("%.1f", n.value);
            ctx.drawText(textRenderer, v, x2 - 8 - textRenderer.getWidth(v), y + 4, Theme.ACCENT_LIGHT, true);
            int sx = x1 + 8, ex = x2 - 8;
            Theme.rrect(ctx, sx, y + 16, ex, y + 20, 2, Theme.OFF);
            int fx = sx + (int) ((ex - sx) * n.fraction());
            Theme.rrect(ctx, sx, y + 16, Math.max(fx, sx + 3), y + 20, 2, Theme.ACCENT);
            Theme.rrect(ctx, fx - 3, y + 14, fx + 3, y + 22, 3, 0xFFFFFFFF);
            add(sx - 3, y + 10, ex + 3, y + 24, (btn, x) -> n.setFromFraction((x - sx) / (double) (ex - sx)), true, top, bot);
            return y + 28;
        }
        if (s instanceof Setting.Hue hu) {
            Theme.rrect(ctx, x1, y, x2, y + 20, 4, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 6, Theme.TEXT, true);
            int sx = x1 + 78, ex = x2 - 30, len = ex - sx;
            for (int i = 0; i < len; i += 2) ctx.fill(sx + i, y + 5, Math.min(sx + i + 2, ex), y + 15, Theme.hue(i / (float) len));
            if (hu.rainbow) ctx.fill(sx, y + 5, ex, y + 15, 0xAA090D13);
            int mxp = sx + (int) (len * hu.hue);
            ctx.fill(mxp - 1, y + 3, mxp + 1, y + 17, 0xFFFFFFFF);
            toggle(ctx, x2 - 26, y + 5, 20, 10, an(hu, hu.rainbow ? 1f : 0f, dt));
            add(sx, y + 3, ex, y + 17, (btn, x) -> { hu.hue = (float) MathHelper.clamp((x - sx) / (double) len, 0, 1); hu.rainbow = false; }, true, top, bot);
            add(x2 - 28, y, x2, y + 20, (btn, x) -> hu.rainbow = !hu.rainbow, false, top, bot);
            return y + 22;
        }
        return y;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double[] loc = toLocal(click.x(), click.y());
        double mx = loc[0], my = loc[1];
        int b = click.button();
        for (int i = regions.size() - 1; i >= 0; i--) {
            Region r = regions.get(i);
            if (in(mx, my, r.x1(), r.y1(), r.x2(), r.y2())) {
                r.act().run(b, mx);
                if (r.drag() && b == 0) dragging = r.act();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll = MathHelper.clamp(scroll - v * 26, 0, maxScroll);
        return true;
    }
}
