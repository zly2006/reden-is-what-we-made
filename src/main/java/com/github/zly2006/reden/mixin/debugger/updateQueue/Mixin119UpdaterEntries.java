package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {
        ChainRestrictedNeighborUpdater.SixWayEntry.class,
        ChainRestrictedNeighborUpdater.SimpleEntry.class,
        ChainRestrictedNeighborUpdater.StatefulEntry.class,
        ChainRestrictedNeighborUpdater.StateReplacementEntry.class,
})
public class Mixin119UpdaterEntries {
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(World world, CallbackInfoReturnable<Boolean> cir) {
        var stage = AbstractBlockUpdateStage.createStage(world.neighborUpdater, (ChainRestrictedNeighborUpdater.Entry) this);
        stage.tick();
        // if we need to cancel
        // todo
    }
}
