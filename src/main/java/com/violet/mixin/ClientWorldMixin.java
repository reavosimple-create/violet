package com.violet.mixin;

import com.violet.module.Modules;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    // require = 0: 1.21.11 moved sky colors toward environment attributes, so this may be skipped.
    @Inject(method = "getSkyColor(Lnet/minecraft/util/math/Vec3d;F)I", at = @At("HEAD"), cancellable = true, require = 0)
    private void violet$sky(Vec3d pos, float delta, CallbackInfoReturnable<Integer> cir) {
        if (Modules.sky.enabled) cir.setReturnValue(Modules.skyColor());
    }
}
