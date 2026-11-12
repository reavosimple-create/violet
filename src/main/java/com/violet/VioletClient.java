package com.violet;

import com.violet.gui.ClickGuiScreen;
import com.violet.gui.MenuTheme;
import com.violet.hud.CrosshairRenderer;
import com.violet.hud.HudRenderer;
import com.violet.hud.Stats;
import com.violet.module.Modules;
import com.violet.util.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import com.violet.hud.FpsBoost;
import net.minecraft.client.gui.screen.DeathScreen;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class VioletClient implements ClientModInitializer {
    public static KeyBinding menuKey, zoomKey;
    private static boolean prevDown, zooming;
    private static int savedFov;

    @Override
    public void onInitializeClient() {
        Modules.init();
        Config.load();

        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("violet", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.violet.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.violet.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, cat));

        ClientTickEvents.END_CLIENT_TICK.register(VioletClient::tick);

        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS,
                Identifier.of("violet", "hud"), HudRenderer::render);
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR,
                orig -> (ctx, tc) -> { if (!CrosshairRenderer.render(ctx)) orig.render(ctx, tc); });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && entity instanceof LivingEntity) Stats.onHit(entity);
            return ActionResult.PASS;
        });

        ScreenEvents.AFTER_INIT.register(MenuTheme::init);
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> FpsBoost.restoreAll());
    }

    private static void tick(MinecraftClient mc) {
        // Right Shift: open in-game, and toggle closed (works on title screen too)
        boolean down = mc.getWindow() != null
                && GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        boolean pressed = false;
        while (menuKey.wasPressed()) pressed = true;
        if (mc.currentScreen == null) {
            if (pressed && mc.player != null) mc.setScreen(new ClickGuiScreen());
        } else if (mc.currentScreen instanceof ClickGuiScreen && down && !prevDown) {
            mc.setScreen(null);
        }
        prevDown = down;
        FpsBoost.tick(mc);

        if (mc.player == null) return;
        Stats.tick(mc);
        if (Modules.autoRespawn.enabled && mc.currentScreen instanceof DeathScreen) mc.player.requestRespawn();

        if (Modules.toggleSprint.enabled && mc.options.forwardKey.isPressed() && !mc.player.isSneaking()
                && !mc.player.horizontalCollision && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }

        boolean wantZoom = Modules.zoom.enabled && mc.currentScreen == null && zoomKey.isPressed();
        if (wantZoom) {
            if (!zooming) { savedFov = mc.options.getFov().getValue(); zooming = true; }
            mc.options.getFov().setValue(Modules.zoomFov.i());
        } else if (zooming) {
            mc.options.getFov().setValue(savedFov);
            zooming = false;
        }
    }
}
