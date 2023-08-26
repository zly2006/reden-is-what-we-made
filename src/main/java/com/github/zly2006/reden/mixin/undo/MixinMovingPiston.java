package com.github.zly2006.reden.mixin.undo;

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

@Mixin(PistonExtensionBlock.class)
public class MixinMovingPiston {
    /**
     * @author zly2006
     * @reason track undo, block entity tick is not the same time as block event tick
     */
    @Overwrite
    @Nullable
    public <T extends PistonBlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (type == BlockEntityType.PISTON) ? (world1, pos, state1, be) -> {
            boolean shouldTick = be.progress >= 1.0f;
            if (shouldTick) {
                if (be instanceof UndoableAccess access) {
                    DebugKt.debugLogger.invoke("Before piston block entity tick: " + pos.toShortString() + ", id" + access.getUndoId());
                    UpdateMonitorHelper.INSTANCE.setRecording(UpdateMonitorHelper.INSTANCE.getUndoRecordsMap().get(access.getUndoId()));
                }
            }
            PistonBlockEntity.tick(world1, pos, state1, be);
            if (shouldTick) {
                if (be instanceof UndoableAccess access) {
                    DebugKt.debugLogger.invoke("After piston block entity tick: " + pos.toShortString() + ", id" + access.getUndoId());
                    UpdateMonitorHelper.INSTANCE.setRecording(null);
                }
            }
        } : null;
    }
}
