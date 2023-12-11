package com.github.zly2006.reden.mixin.debugger.updateQueue;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = {
        ChainRestrictedNeighborUpdater.SixWayEntry.class,
        ChainRestrictedNeighborUpdater.SimpleEntry.class,
        ChainRestrictedNeighborUpdater.StatefulEntry.class,
        ChainRestrictedNeighborUpdater.StateReplacementEntry.class,
}, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class Mixin119UpdaterEntries implements TickStageOwnerAccess {
    @Unique boolean ticked = false;
    @Unique AbstractBlockUpdateStage<?> stage;

    public boolean getTicked$reden() {
        return ticked;
    }

    public void setTicked$reden(boolean ticked) {
        this.ticked = ticked;
    }

    public @NotNull AbstractBlockUpdateStage<?> getTickStage$reden() {
        return stage;
    }

    public void setTickStage$reden(TickStage tickStage) {
        stage = (AbstractBlockUpdateStage<?>) tickStage;
    }
}
