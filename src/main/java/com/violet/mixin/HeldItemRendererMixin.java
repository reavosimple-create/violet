package com.violet.mixin;

import com.violet.module.Modules;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * View Model: moves/rotates/scales your first-person hands and held items.
 * Matches the MatrixStack argument by type, so it keeps working if the other parameters change.
 * require = 0 means the game still launches if the method is renamed in a future version.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Unique private MatrixStack violet$pending;

    @ModifyVariable(method = "renderFirstPersonItem", at = @At("HEAD"), argsOnly = true, require = 0)
    private MatrixStack violet$pre(MatrixStack m) {
        if (!Modules.viewModel.enabled) return m;
        m.push();
        m.translate(Modules.vmX.i() / 100f, Modules.vmY.i() / 100f, Modules.vmZ.i() / 100f);
        m.translate(0f, -0.5f, -0.7f); // pivot near the hand so rotation/scale feel natural
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Modules.vmRotX.i()));
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(Modules.vmRotY.i()));
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(Modules.vmRotZ.i()));
        float s = Modules.vmScale.i() / 100f;
        m.scale(s, s, s);
        m.translate(0f, 0.5f, 0.7f);
        violet$pending = m;
        return m;
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"), require = 0)
    private void violet$post(CallbackInfo ci) {
        if (violet$pending != null) { violet$pending.pop(); violet$pending = null; }
    }
}
