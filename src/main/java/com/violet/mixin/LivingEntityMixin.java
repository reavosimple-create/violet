package com.violet.mixin;

import com.violet.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    // require = 0: if this method changes in a future version the game still launches.
    @Inject(method = "getHandSwingDuration()I", at = @At("HEAD"), cancellable = true, require = 0)
    private void violet$swing(CallbackInfoReturnable<Integer> cir) {
        if (Modules.swing.enabled && (Object) this == MinecraftClient.getInstance().player) {
            cir.setReturnValue(Modules.swingTicks.i());
        }
    }
}
