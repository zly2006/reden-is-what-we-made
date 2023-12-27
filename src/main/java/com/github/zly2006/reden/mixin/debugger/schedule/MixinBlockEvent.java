package com.github.zly2006.reden.mixin.debugger.schedule;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageTreeOwnerAccess;
import com.github.zly2006.reden.debugger.tree.TickStageTree;
import net.minecraft.server.world.BlockEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BlockEvent.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinBlockEvent implements TickStageTreeOwnerAccess {
    @Unique @Nullable TickStageTree occurred;

    @Nullable
    @Override
    public TickStageTree getTickStageTree$reden() {
        return occurred;
    }

    @Override
    public void setTickStageTree$reden(@Nullable TickStageTree tickStageTree) {
        occurred = tickStageTree;
    }
}
