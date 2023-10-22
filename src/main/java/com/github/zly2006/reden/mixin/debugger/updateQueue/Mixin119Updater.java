package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.access.UpdaterData;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.github.zly2006.reden.access.ServerData.data;
import static com.github.zly2006.reden.access.UpdaterData.updaterData;

@Mixin(ChainRestrictedNeighborUpdater.class)
public abstract class Mixin119Updater implements NeighborUpdater {
    @Shadow private int depth;

    @Shadow @Final private World world;

    @Inject(method = "runQueuedUpdates", at = @At("HEAD"))
    private void onRunQueuedUpdates(CallbackInfo ci) {
        if (!world.isClient) {
            UpdaterData updaterData = updaterData(this);
            ServerData serverData = data(Objects.requireNonNull(world.getServer(), "R-Debugger is not available on clients!"));
            updaterData.setCurrentParentTickStage(serverData.getTickStageTree().peekLeaf());
        }
    }
}
