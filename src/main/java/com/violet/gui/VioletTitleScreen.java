package com.violet.gui;

import com.violet.util.Logo;
import com.violet.util.Theme;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/** Replacement main menu: animated glacier background, floating particles, staggered button entrance. */
public class VioletTitleScreen extends Screen {
    private static final String[] LABELS = {"Singleplayer", "Multiplayer", "Options", "Violet Settings", "Quit Game"};
    private static final int N = 90, BW = 220, BH = 24, GAP = 7;

    private final long openAt = System.nanoTime();
    private long lastFrame = openAt;
    private final float[] px = new float[N], py = new float[N], ps = new float[N], pz = new float[N], pp = new float[N];
    private final float[] hover = new float[LABELS.length];
    private final int[][] rects = new int[LABELS.length][4];
    private final float[] shown = new float[LABELS.length];

    public VioletTitleScreen() {
        super(Text.literal("Violet"));
        Random r = new Random(1337);
        for (int i = 0; i < N; i++) {
            px[i] = r.nextFloat();
            py[i] = r.nextFloat();
            ps[i] = 0.012f + r.nextFloat() * 0.035f;
            pz[i] = r.nextFloat();
            pp[i] = 0.6f + r.nextFloat() * 1.8f;
        }
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean shouldPause() { return false; }

    private static float frac(float v) { return v - (float) Math.floor(v); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1e9f);
        lastFrame = now;
        float t = (now - openAt) / 1e9f;
        int w = width, h = height, cx = w / 2;

        // ---- background ----
        ctx.fillGradient(0, 0, w, h, 0xFF04070B, 0xFF0A1824);
        float pulse = 0.5f + 0.5f * (float) Math.sin(t * 0.7f);
        ctx.fillGradient(0, h / 2, w, h, 0x00000000, Theme.alpha(Theme.ACCENT, (int) (0x20 + 0x14 * pulse)));
        for (int i = 0; i < 5; i++) {
            float bx = frac(i * 0.23f + t * 0.006f * (1 + i % 3)) * (w + 120) - 60;
            int bw = 34 + i * 17;
            ctx.fillGradient((int) bx, 0, (int) bx + bw, h, 0x00000000, Theme.alpha(Theme.ACCENT, 0x0F));
        }
        float ox = (mx - w / 2f);
        for (int i = 0; i < N; i++) {
            float x = frac(px[i] + t * 0.004f * pz[i]) * w - ox * pz[i] * 0.03f;
            float y = h - frac(py[i] + t * ps[i]) * h;
            float tw = 0.35f + 0.65f * Math.abs((float) Math.sin(t * pp[i] + pp[i] * 6f));
            int size = pz[i] > 0.7f ? 2 : 1;
            int a = (int) (tw * (60 + 140 * pz[i]));
            if (a < 6) continue;
            ctx.fill((int) x, (int) y, (int) x + size, (int) y + size, Theme.alpha(Theme.ACCENT_LIGHT, a));
        }
        ctx.fillGradient(0, 0, w, 56, 0xA0020509, 0x00000000);
        ctx.fillGradient(0, h - 56, w, h, 0x00000000, 0xB0020509);

        // ---- layout ----
        int logo = h < 300 ? 36 : 52;
        int blockH = logo + 8 + 27 + 18 + 18 + LABELS.length * BH + (LABELS.length - 1) * GAP;
        int top = Math.max(6, (h - blockH) / 2 - 4);

        // ---- logo + wordmark ----
        float intro = Theme.easeOutCubic(Math.min(1f, t / 0.6f));
        int ly = top + (int) (Math.sin(t * 1.5f) * 2) - (int) ((1f - intro) * 10f);
        for (int i = 1; i <= 5; i++) {
            Theme.rrect(ctx, cx - logo / 2 - i * 3, ly - i * 3, cx + logo / 2 + i * 3, ly + logo + i * 3,
                    logo / 2 + i * 3, Theme.fade(Theme.alpha(Theme.ACCENT, 0x0A), intro));
        }
        if (intro > 0.05f) Logo.draw(ctx, cx - logo / 2, ly, logo);

        int wy = top + logo + 8;
        Text wm = Text.literal("VIOLET").formatted(Formatting.BOLD);
        int tw = textRenderer.getWidth(wm);
        if (intro > 0.1f) {
            ctx.getMatrices().pushMatrix();
            ctx.getMatrices().translate(cx, wy);
            ctx.getMatrices().scale(3f, 3f);
            ctx.drawText(textRenderer, wm, -tw / 2, 0, Theme.fade(0xFFFFFFFF, intro), true);
            ctx.getMatrices().popMatrix();
        }
        int uw = (int) (34 * intro);
        ctx.fill(cx - uw, wy + 29, cx + uw, wy + 30, Theme.ACCENT);
        String tag = "PVP UTILITY CLIENT";
        if (intro > 0.3f) ctx.drawText(textRenderer, tag, cx - textRenderer.getWidth(tag) / 2, wy + 36, Theme.fade(Theme.DIM, intro), false);

        // ---- buttons ----
        int by = top + logo + 8 + 27 + 18 + 18;
        for (int i = 0; i < LABELS.length; i++) {
            float a = Theme.easeOutCubic(Math.min(1f, Math.max(0f, (t - 0.25f - i * 0.07f) / 0.4f)));
            shown[i] = a;
            int x1 = cx - BW / 2, y1 = by + i * (BH + GAP) + (int) ((1f - a) * 12f);
            rects[i][0] = x1; rects[i][1] = y1; rects[i][2] = x1 + BW; rects[i][3] = y1 + BH;
            if (a < 0.04f) continue;
            boolean hov = a > 0.9f && mx >= x1 && mx < x1 + BW && my >= y1 && my < y1 + BH;
            hover[i] = Theme.approach(hover[i], hov ? 1f : 0f, dt, 16f);
            float hv = hover[i];
            boolean quit = i == LABELS.length - 1;
            int accent = quit ? Theme.BAD : Theme.ACCENT;
            int shift = (int) (hv * 4);
            Theme.rrectBorder(ctx, x1, y1, x1 + BW, y1 + BH, 6,
                    Theme.fade(Theme.lerp(0xE00D141D, Theme.alpha(accent, 0x38), hv), a),
                    Theme.fade(Theme.lerp(0x40FFFFFF, Theme.alpha(accent, 0xB0), hv), a));
            if (hv > 0.02f) Theme.rrect(ctx, x1 + 6, y1 + 6, x1 + 8, y1 + BH - 6, 1, Theme.fade(accent, hv * a));
            int tx = cx - textRenderer.getWidth(LABELS[i]) / 2 + shift / 2;
            ctx.drawText(textRenderer, LABELS[i], tx, y1 + 8, Theme.fade(Theme.lerp(0xFFB9C7D6, 0xFFFFFFFF, hv), a), true);
        }

        // ---- footer ----
        float fa = Theme.easeOutCubic(Math.min(1f, Math.max(0f, (t - 0.7f) / 0.5f)));
        ctx.drawText(textRenderer, "Minecraft 1.21.11", 8, h - 14, Theme.fade(Theme.DIM, fa), false);
        String r = "Right Shift opens the Violet menu in-game";
        ctx.drawText(textRenderer, r, w - 8 - textRenderer.getWidth(r), h - 14, Theme.fade(Theme.DIM, fa), false);
    }

    private void click(int i) {
        client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        switch (i) {
            case 0 -> client.setScreen(new SelectWorldScreen(this));
            case 1 -> client.setScreen(new MultiplayerScreen(this));
            case 2 -> client.setScreen(new OptionsScreen(this, client.options));
            case 3 -> client.setScreen(new ClickGuiScreen());
            default -> client.scheduleStop();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            double mx = click.x(), my = click.y();
            for (int i = 0; i < LABELS.length; i++) {
                int[] r = rects[i];
                if (shown[i] > 0.9f && mx >= r[0] && mx < r[2] && my >= r[1] && my < r[3]) {
                    click(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }
}
