package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.UpdaterData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry;
import net.minecraft.world.block.NeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.List;

@Mixin(value = ChainRestrictedNeighborUpdater.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class Mixin119Updater implements NeighborUpdater, UpdaterData.UpdaterDataAccess {
    @Shadow private int depth;

    @Shadow @Final private World world;

    @Shadow @Final private ArrayDeque<Entry> queue;

    @Shadow @Final private List<Entry> pending;

    @Shadow protected abstract void runQueuedUpdates();

    @Unique private final UpdaterData updaterData = new UpdaterData(this);

    @Override
    public void yieldUpdater() {
        // already overridden
        runQueuedUpdates();
    }

    @NotNull
    @Override
    public UpdaterData getRedenUpdaterData() {
        return updaterData;
    }

    @Inject(
            method = "enqueue",
            at = @At("HEAD")
    )
    private void onNewEntry(BlockPos pos, Entry entry, CallbackInfo ci) {
        if (!world.isClient && // don't inject on the client
                RedenCarpetSettings.Debugger.debuggerBlockUpdates()) {
            AbstractBlockUpdateStage.createAndInsert(this, entry);
        }
    }

    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Inject(
            method = "runQueuedUpdates",
            at = @At("HEAD"),
            cancellable = true
    )
    public final void onRunQueuedUpdates(CallbackInfo ci) {
        if (updaterData.notifyMixinsOnly) {
            notifyMixins(ci);
        } else if (!world.isClient && RedenCarpetSettings.Debugger.debuggerBlockUpdates()) {
            redirectToStage(ci);
        }
    }

    private void notifyMixins(CallbackInfo ci) {
        if (world.isClient) {
            throw new RuntimeException("Ticking updates by stages at client");
        }
        // To keep injecting points, we need to call the original method
        // notify mixins only

        // Note: This variable is used to let other mods locate injecting point
        Entry entry = updaterData.getTickingEntry();
        entry.update(null); // Note: this should be noop (let it throw exception if not)

        updaterData.tickingStage = null;
        updaterData.notifyMixinsOnly = false;
        ci.cancel(); // processing entry ends here
    }

    private void redirectToStage(CallbackInfo ci) {
        updaterData.getTickStageTree().next().tick();
        ci.cancel();
    }
}
