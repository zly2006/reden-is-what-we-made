package com.github.zly2006.reden.mixin.debugger.updater;

import com.github.zly2006.reden.access.TickStageTreeOwnerAccess;
import com.github.zly2006.reden.debugger.tree.TickStageTree;
import net.minecraft.world.tick.OrderedTick;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OrderedTick.class)
public class MixinOrderedTick implements TickStageTreeOwnerAccess {
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
