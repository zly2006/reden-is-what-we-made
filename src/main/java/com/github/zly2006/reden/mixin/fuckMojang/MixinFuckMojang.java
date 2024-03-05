package com.github.zly2006.reden.mixin.fuckMojang;

import com.github.zly2006.reden.Reden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.network.BlockListChecker$1")
public class MixinFuckMojang {
    @Inject(
            method = "isAllowed*",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fuck(CallbackInfoReturnable<Boolean> cir) {
        if (Reden.isRedenDev()) cir.setReturnValue(true);
    }
}
