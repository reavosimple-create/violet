package com.violet.util;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class Logo {
    private static final Identifier ID = Identifier.of("violet", "textures/gui/logo.png");

    /** Draws the Violet logo (128x128 texture) scaled to size x size. */
    public static void draw(DrawContext ctx, int x, int y, int size) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, ID, x, y, 0f, 0f, size, size, 128, 128, 128, 128);
    }
}
