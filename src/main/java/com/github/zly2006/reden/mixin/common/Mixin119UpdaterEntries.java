package com.github.zly2006.reden.mixin.common;


import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = {
        ChainRestrictedNeighborUpdater.SixWayEntry.class,
        ChainRestrictedNeighborUpdater.SimpleEntry.class,
        ChainRestrictedNeighborUpdater.StatefulEntry.class,
        ChainRestrictedNeighborUpdater.StateReplacementEntry.class,
})
public class Mixin119UpdaterEntries implements TickStageOwnerAccess {
    @Unique AbstractBlockUpdateStage<?> stage;

    public @NotNull AbstractBlockUpdateStage<?> getTickStage() {
        return stage;
    }

    @Override
    public void setTickStage(@NotNull TickStage tickStage) {
        stage = (AbstractBlockUpdateStage<?>) tickStage;
    }
}
