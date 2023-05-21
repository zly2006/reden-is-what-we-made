package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.ChainedUpdaterView;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class MixinUpdater implements ChainedUpdaterView {
    @Shadow @Final private World world;
    @Shadow private int depth;
    @Mutable @Shadow @Final private int maxChainDepth;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(World world, int maxChainDepth, CallbackInfo ci) {
        int a = MalilibSettingsKt.getMAX_CHAIN_UPDATES().getIntegerValue();
        if (a != -1) {
            this.maxChainDepth = a;
        }
    }
    @Inject(method = "runQueuedUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/block/ChainRestrictedNeighborUpdater$Entry;update(Lnet/minecraft/world/World;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRunQueuedUpdates(CallbackInfo ci, ChainRestrictedNeighborUpdater.Entry entry) {
        UpdateMonitorHelper.onUpdate(world, entry);
    }
    @Inject(method = "runQueuedUpdates", at = @At("RETURN"))
    private void finishUpdates(CallbackInfo ci) {
        UpdateMonitorHelper.onChainFinish(world);
    }
}
