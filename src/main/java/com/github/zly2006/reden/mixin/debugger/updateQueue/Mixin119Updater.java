package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.access.UpdaterData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.debugger.TickStage;
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

    @Unique private final UpdaterData updaterData = new UpdaterData(this);

    @Unique boolean shouldEntryContinue = false;

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
                RedenCarpetSettings.redenDebuggerBlockUpdates &&
                RedenCarpetSettings.redenDebuggerEnabled) {
            AbstractBlockUpdateStage.createStage(this, entry);
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public final void runQueuedUpdates() {
        if (updaterData.thenTickUpdate) {
            // To keep injecting points, we need to call the original method
            // Call this method with `thenTickUpdate` = true to tick update

            // Note: This variable is used to let other mods locate injecting point
            Entry entry = updaterData.getTickingEntry();
            shouldEntryContinue = entry.update(this.world);

            updaterData.tickingStage = null;
            updaterData.thenTickUpdate = false;
            return; // processing entry ends here
        }

        try {
            while (!this.queue.isEmpty() || !this.pending.isEmpty()) {
                for (int i = this.pending.size() - 1; i >= 0; --i) {
                    this.queue.push(this.pending.get(i));
                }

                this.pending.clear();

                // Note: This variable is used to let other mods locate injecting point
                Entry entry = this.queue.peek();

                while (this.pending.isEmpty()) {
                    // Reden start
                    if (!world.isClient && // don't inject on the client
                            RedenCarpetSettings.redenDebuggerBlockUpdates &&
                            RedenCarpetSettings.redenDebuggerEnabled) {
                        // do tick by our method
                        TickStage stage = ((TickStageOwnerAccess) entry).getTickStage();
                        updaterData.appendStage(stage);
                        updaterData.tickNextStage();
                    } else {
                        // do tick by original method
                        shouldEntryContinue = entry.update(this.world);
                    }

                    if (!shouldEntryContinue) {
                        // Reden stop

                        // Note: call update multiple times is only used by six-way entries
                        this.queue.pop();
                        break;
                    }
                }
            }
        } finally {
            this.queue.clear();
            this.pending.clear();
            this.depth = 0;
        }
    }
}
