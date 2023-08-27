package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.UndoRecordContainerImpl;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonExtensionBlock;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PistonExtensionBlock.class)
public class MixinMovingPiston {
    @Unique UndoRecordContainerImpl recordContainer = new UndoRecordContainerImpl();

    /**
     * @author zly2006
     * @reason track undo, block entity tick is not the same time as block event tick
     */
    @Overwrite
    @Nullable
    public <T extends PistonBlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (type == BlockEntityType.PISTON) ? (world1, pos, state1, be) -> {
            boolean shouldTick = be.getProgress(1) >= 1.0f; // current progress, delta=1
            if (shouldTick) {
                if (be instanceof UndoableAccess access) {
                    DebugKt.debugLogger.invoke("Before piston block entity tick: " + pos.toShortString() + ", id" + access.getUndoId());
                    recordContainer.setId(access.getUndoId());
                    UpdateMonitorHelper.INSTANCE.swap(recordContainer);
                }
            }
            PistonBlockEntity.tick(world1, pos, state1, be);
            if (shouldTick) {
                if (be instanceof UndoableAccess access) {
                    DebugKt.debugLogger.invoke("After piston block entity tick: " + pos.toShortString() + ", id" + access.getUndoId());
                    UpdateMonitorHelper.INSTANCE.swap(recordContainer);
                }
            }
        } : null;
    }
}
