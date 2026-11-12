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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClickGuiScreen extends Screen {
    private interface Act { void run(int button, double mx); }
    private record Region(int x1, int y1, int x2, int y2, Act act, boolean drag) {}

    private static final int W = 400, H = 250, SIDE = 86;
    private final List<Region> regions = new ArrayList<>();
    private final Set<Module> open = new HashSet<>();
    private Module.Category tab = Module.Category.RENDER;
    private Act dragging;
    private double scroll, maxScroll;
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

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        regions.clear();
        hoverDesc = "";
        long handle = client.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (dragging != null) { if (down) dragging.run(0, mx); else dragging = null; }

        ctx.fill(0, 0, width, height, 0xC0000000);
        int x0 = (width - W) / 2, y0 = (height - H) / 2;
        ctx.fill(x0 - 1, y0 - 1, x0 + W + 1, y0 + H + 1, Theme.ACCENT_DARK);
        ctx.fill(x0, y0, x0 + W, y0 + H, Theme.BG);
        ctx.fill(x0, y0, x0 + W, y0 + 22, Theme.PANEL);
        ctx.fill(x0, y0 + 21, x0 + W, y0 + 22, Theme.ACCENT);
        Logo.draw(ctx, x0 + 6, y0 + 3, 16);
        ctx.drawText(textRenderer, Text.literal("VIOLET").formatted(Formatting.BOLD), x0 + 26, y0 + 7, 0xFFFFFFFF, true);
        String hint = "Right Shift to close";
        ctx.drawText(textRenderer, hint, x0 + W - 8 - textRenderer.getWidth(hint), y0 + 7, Theme.ACCENT_LIGHT, true);

        // sidebar
        ctx.fill(x0, y0 + 22, x0 + SIDE, y0 + H, Theme.PANEL);
        int ty = y0 + 30;
        for (Module.Category c : Module.Category.values()) {
            boolean sel = c == tab, hov = in(mx, my, x0 + 6, ty, x0 + SIDE - 4, ty + 22);
            ctx.fill(x0 + 6, ty, x0 + SIDE - 4, ty + 22, sel ? Theme.ACCENT_DARK : hov ? Theme.ROW_HOVER : Theme.ROW);
            if (sel) ctx.fill(x0 + 6, ty, x0 + 8, ty + 22, Theme.ACCENT_LIGHT);
            ctx.drawText(textRenderer, c.label, x0 + 14, ty + 7, sel ? 0xFFFFFFFF : Theme.DIM, true);
            final Module.Category cc = c;
            regions.add(new Region(x0 + 6, ty, x0 + SIDE - 4, ty + 22, (b, x) -> { tab = cc; scroll = 0; }, false));
            ty += 26;
        }

        // content
        int cx1 = x0 + SIDE + 6, cx2 = x0 + W - 8, top = y0 + 28, bot = y0 + H - 18;
        ctx.enableScissor(cx1 - 2, top, cx2 + 2, bot);
        int y = top - (int) scroll;
        for (Module m : Modules.in(tab)) y = module(ctx, m, cx1, cx2, y, mx, my, top, bot);
        ctx.disableScissor();
        maxScroll = Math.max(0, (y + (int) scroll) - bot);
        scroll = MathHelper.clamp(scroll, 0, maxScroll);

        long on = Modules.ALL.stream().filter(m -> m.enabled).count();
        ctx.drawText(textRenderer, hoverDesc.isEmpty() ? on + " modules enabled  |  left click: toggle  |  right click: settings" : hoverDesc,
                x0 + SIDE + 8, y0 + H - 12, Theme.DIM, true);
    }

    private int module(DrawContext ctx, Module m, int x1, int x2, int y, int mx, int my, int top, int bot) {
        int h = 20;
        boolean hov = in(mx, my, x1, y, x2, y + h) && my >= top && my < bot;
        if (hov) hoverDesc = m.desc;
        ctx.fill(x1, y, x2, y + h, hov ? Theme.ROW_HOVER : Theme.ROW);
        if (m.enabled) ctx.fill(x1, y, x1 + 2, y + h, Theme.ACCENT);
        ctx.drawText(textRenderer, m.name, x1 + 8, y + 6, m.enabled ? 0xFFFFFFFF : Theme.DIM, true);
        if (!m.settings.isEmpty()) ctx.drawText(textRenderer, open.contains(m) ? "v" : ">", x2 - 50, y + 6, Theme.DIM, true);
        String st = m.enabled ? "ON" : "OFF";
        ctx.fill(x2 - 34, y + 4, x2 - 6, y + 16, m.enabled ? Theme.ACCENT_DARK : 0xFF2A1450);
        ctx.drawText(textRenderer, st, x2 - 20 - textRenderer.getWidth(st) / 2, y + 6, 0xFFFFFFFF, true);
        add(x1, y, x2, y + h, (b, x) -> {
            if (b == 0) m.toggle();
            else if (b == 1 && !m.settings.isEmpty()) { if (!open.remove(m)) open.add(m); }
        }, false, top, bot);
        y += h + 2;
        if (open.contains(m)) for (Setting s : m.settings) y = setting(ctx, s, x1 + 6, x2, y, mx, my, top, bot);
        return y;
    }

    private int setting(DrawContext ctx, Setting s, int x1, int x2, int y, int mx, int my, int top, int bot) {
        if (s instanceof Setting.Bool b) {
            ctx.fill(x1, y, x2, y + 16, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 4, Theme.TEXT, true);
            box(ctx, x2 - 18, y + 3, b.value);
            add(x1, y, x2, y + 16, (btn, x) -> b.value = !b.value, false, top, bot);
            return y + 17;
        }
        if (s instanceof Setting.Mode md) {
            ctx.fill(x1, y, x2, y + 16, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 4, Theme.TEXT, true);
            String v = "< " + md.get() + " >";
            ctx.drawText(textRenderer, v, x2 - 8 - textRenderer.getWidth(v), y + 4, Theme.ACCENT_LIGHT, true);
            add(x1, y, x2, y + 16, (btn, x) -> md.cycle(btn == 1 ? -1 : 1), false, top, bot);
            return y + 17;
        }
        if (s instanceof Setting.Num n) {
            ctx.fill(x1, y, x2, y + 24, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 3, Theme.TEXT, true);
            String v = n.step >= 1 ? String.valueOf(n.i()) : String.format("%.1f", n.value);
            ctx.drawText(textRenderer, v, x2 - 8 - textRenderer.getWidth(v), y + 3, Theme.ACCENT_LIGHT, true);
            int sx = x1 + 8, ex = x2 - 8;
            ctx.fill(sx, y + 15, ex, y + 19, 0xFF2A1450);
            int fx = sx + (int) ((ex - sx) * n.fraction());
            ctx.fill(sx, y + 15, fx, y + 19, Theme.ACCENT);
            ctx.fill(fx - 2, y + 12, fx + 2, y + 22, Theme.ACCENT_LIGHT);
            add(sx - 2, y + 9, ex + 2, y + 23, (btn, x) -> n.setFromFraction((x - sx) / (double) (ex - sx)), true, top, bot);
            return y + 25;
        }
        if (s instanceof Setting.Hue hu) {
            ctx.fill(x1, y, x2, y + 18, Theme.SUB);
            ctx.drawText(textRenderer, s.name, x1 + 8, y + 5, Theme.TEXT, true);
            int sx = x1 + 78, ex = x2 - 26, len = ex - sx;
            for (int i = 0; i < len; i += 2) ctx.fill(sx + i, y + 4, Math.min(sx + i + 2, ex), y + 14, Theme.hue(i / (float) len));
            if (hu.rainbow) ctx.fill(sx, y + 4, ex, y + 14, 0x99120822);
            int mxp = sx + (int) (len * hu.hue);
            ctx.fill(mxp - 1, y + 2, mxp + 1, y + 16, 0xFFFFFFFF);
            box(ctx, x2 - 18, y + 4, hu.rainbow);
            ctx.drawText(textRenderer, "R", x2 - 15, y + 5, hu.rainbow ? 0xFFFFFFFF : Theme.DIM, false);
            add(sx, y + 2, ex, y + 16, (btn, x) -> { hu.hue = (float) MathHelper.clamp((x - sx) / (double) len, 0, 1); hu.rainbow = false; }, true, top, bot);
            add(x2 - 20, y, x2, y + 18, (btn, x) -> hu.rainbow = !hu.rainbow, false, top, bot);
            return y + 19;
        }
        return y;
    }

    private void box(DrawContext ctx, int x, int y, boolean on) {
        ctx.fill(x, y, x + 10, y + 10, 0xFF3A1D66);
        if (on) ctx.fill(x + 2, y + 2, x + 8, y + 8, Theme.ACCENT);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
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
        scroll = MathHelper.clamp(scroll - v * 18, 0, maxScroll);
        return true;
    }
}
