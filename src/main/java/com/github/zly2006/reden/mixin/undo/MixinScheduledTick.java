package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.UndoableAccess;
import net.minecraft.world.tick.OrderedTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(OrderedTick.class)
public class MixinScheduledTick implements UndoableAccess {
    @Unique
    long undoId;

    @Override
    public long getUndoId$reden() {
        return undoId;
    }

    @Override
    public void setUndoId$reden(long l) {
        undoId = l;
    }
}
