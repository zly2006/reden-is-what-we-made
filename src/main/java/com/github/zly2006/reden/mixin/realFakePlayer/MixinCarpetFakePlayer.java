package com.github.zly2006.reden.mixin.realFakePlayer;

import carpet.patches.EntityPlayerMPFake;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMPFake.class)
public class MixinCarpetFakePlayer {
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcarpet/patches/EntityPlayerMPFake;playerTick()V"
            ),
            cancellable = true
    )
    private void onTick(CallbackInfo ci) {
        if (RedenCarpetSettings.Options.realFakePlayer) {
            ci.cancel(); // quit before playerTick()
        }
    }
}
