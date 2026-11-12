package com.violet.gui;

import com.violet.hud.DeathFx;
import com.violet.module.Modules;
import com.violet.util.Logo;
import com.violet.util.Theme;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class MenuTheme {
    public static void init(MinecraftClient client, Screen screen, int w, int h) {
        if (screen instanceof DeathScreen) {
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> {
                DeathFx.render(ctx, s.width, s.height);
                DeathFx.renderInfo(ctx, client.textRenderer, s.width, s.height);
            });
            return;
        }
        if (!Modules.menuTheme.enabled) {
            if (screen instanceof TitleScreen) {
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Violet Menu"),
                        b -> client.setScreen(new ClickGuiScreen())).dimensions(6, 6, 90, 20).build());
            }
            return;
        }
        if (screen instanceof GameMenuScreen || screen instanceof OptionsScreen
                || screen instanceof SelectWorldScreen || screen instanceof MultiplayerScreen) {
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> {
                ctx.fillGradient(0, 0, s.width, 44, 0x90050A10, 0x00000000);
                ctx.fillGradient(0, s.height - 44, s.width, s.height, 0x00000000, 0x90050A10);
                ctx.fill(0, 0, s.width, 1, Theme.alpha(Theme.ACCENT, 0xB0));
                Logo.draw(ctx, 6, s.height - 24, 16);
            });
        }
    }
}
