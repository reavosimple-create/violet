package com.violet.gui;

import com.violet.module.Modules;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
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
        if (!Modules.menuTheme.enabled) return;
        if (screen instanceof TitleScreen) {
            Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Violet Menu"),
                    b -> client.setScreen(new ClickGuiScreen())).dimensions(6, 6, 90, 20).build());
        }
        if (screen instanceof TitleScreen || screen instanceof GameMenuScreen || screen instanceof OptionsScreen
                || screen instanceof SelectWorldScreen || screen instanceof MultiplayerScreen) {
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, delta) -> {
                ctx.fillGradient(0, 0, s.width, s.height, 0x55000000, 0x99200050);
                com.violet.util.Logo.draw(ctx, 6, s.height - 30, 24);
                ctx.fill(0, 0, s.width, 2, 0xFFA855F7);
                ctx.fill(0, s.height - 2, s.width, s.height, 0xFF6D28D9);
            });
        }
    }
}
