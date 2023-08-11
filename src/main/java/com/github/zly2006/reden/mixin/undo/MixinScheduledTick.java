package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.ScheduledTickAccess;
import net.minecraft.world.tick.OrderedTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OrderedTick.class)
public class MixinScheduledTick implements ScheduledTickAccess {
    @Unique
    long undoId;

    @Override
    public long getUndoId() {
        return undoId;
    }

    @Override
    public void setUndoId(long l) {
        undoId = l;
    }
}
