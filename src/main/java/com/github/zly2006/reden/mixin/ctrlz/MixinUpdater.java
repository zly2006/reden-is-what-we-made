package com.github.zly2006.reden.mixin.ctrlz;

import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class MixinUpdater {
    @Inject(method = "runQueuedUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater$Entry;update(Lnet/minecraft/world/World;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRunQueuedUpdates(CallbackInfo ci, ChainRestrictedNeighborUpdater.Entry entry) {

    }
}
