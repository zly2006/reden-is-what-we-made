package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.access.UpdaterData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.debugger.stages.UpdateBlockStage;
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry;
import net.minecraft.world.block.NeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;

import static com.github.zly2006.reden.access.ServerData.data;
import static com.github.zly2006.reden.access.UpdaterData.updaterData;

@Mixin(value = ChainRestrictedNeighborUpdater.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class Mixin119Updater implements NeighborUpdater, UpdaterData.UpdaterDataAccess {
    @Shadow private int depth;

    @Shadow @Final private World world;

    @Shadow @Final private ArrayDeque<Entry> queue;

    @Shadow @Final private List<Entry> pending;

    @Unique private final UpdaterData updaterData = new UpdaterData(this);

    @Unique boolean shouldEntryStop = false;

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
            shouldEntryStop = entry.update(this.world);

            updaterData.tickingStage = null;
            updaterData.thenTickUpdate = false;
        }
        else {
            beforeUpdate();
            try {
                while (!this.queue.isEmpty() || !this.pending.isEmpty()) {
                    for (int i = this.pending.size() - 1; i >= 0; --i) {
                        this.queue.push(this.pending.get(i));
                    }

                    this.pending.clear();

                    // Note: This variable is used to let other mods locate injecting point
                    Entry entry = this.queue.peek();

                    while (this.pending.isEmpty()) {
                        updaterData.getTickStageTree().assertInTree(updaterData.currentParentTickStage);
                        // Reden start
                        if (RedenCarpetSettings.redenDebuggerBlockUpdates &&
                                RedenCarpetSettings.redenDebuggerEnabled) {
                            // do tick by our method
                            var stage = AbstractBlockUpdateStage.createStage(this, entry);
                            updaterData.appendStage(stage);
                            updaterData.tickNextStage();
                        } else {
                            // do tick by original method
                            shouldEntryStop = entry.update(this.world);
                        }

                        if (!shouldEntryStop) {
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

                if (updaterData.currentParentTickStage != null) {
                    updaterData.getTickStageTree().assertInTree(updaterData.currentParentTickStage);
                }
                System.out.println("afterUpdate");
                updaterData.currentParentTickStage = null;
            }
        }
    }

    @Unique private void beforeUpdate() {
        if (!world.isClient) {
            System.out.println("beforeUpdate");
            UpdaterData updaterData = updaterData(this);
            ServerData serverData = data(Objects.requireNonNull(world.getServer(), "R-Debugger is not available on clients!"));
            updaterData.currentParentTickStage = new UpdateBlockStage(serverData.getTickStageTree().peekLeaf());
            updaterData.getTickStageTree().insert2child(updaterData.currentParentTickStage);

            updaterData.getTickStageTree().assertInTree(updaterData.currentParentTickStage);
        }
    }
}
