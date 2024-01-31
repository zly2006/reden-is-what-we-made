package com.github.zly2006.reden.mixin.realFakePlayer;

import carpet.helpers.EntityPlayerActionPack;
import com.github.zly2006.reden.access.IEntityPlayerActionPack;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerActionPack.class)
public class MixinEntityPlayerActionPack implements IEntityPlayerActionPack {
    @Unique
    boolean isNetworkPhase = false;

    @Inject(
            method = "onUpdate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUpdate(CallbackInfo ci) {
        if (RedenCarpetSettings.Options.realFakePlayer && !isNetworkPhase) {
            ci.cancel();
        }
    }

    @Override
    public void setNetworkPhase$reden(boolean networkPhase) {
        isNetworkPhase = networkPhase;
    }

    @Override
    public boolean isNetworkPhase$reden() {
        return isNetworkPhase;
    }
}
