package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.ScheduledTickAccess;
import net.minecraft.server.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(BlockEvent.class)
public class MixinBlockEvent implements ScheduledTickAccess {
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
