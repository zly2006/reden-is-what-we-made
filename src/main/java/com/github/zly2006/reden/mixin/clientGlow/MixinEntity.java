package com.github.zly2006.reden.mixin.clientGlow;

import com.github.zly2006.reden.clientGlow.ClientGlowKt;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {
    @Inject(
            method = "isGlowing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (MalilibSettingsKt.ENABLE_CLIENT_GLOW.getBooleanValue()) {
            cir.setReturnValue(ClientGlowKt.getGlowing().contains(this));
        }
    }
}
