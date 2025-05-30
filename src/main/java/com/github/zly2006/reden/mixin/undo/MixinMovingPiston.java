package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MovingPistonBlock.class)
public class MixinMovingPiston {
    /**
     * @author zly2006
     * @reason track undo, block entity tick is not the same time as block event tick
     */
    @Overwrite
    @Nullable
    public BlockEntityTicker<PistonMovingBlockEntity> getTicker(Level level, BlockState blockState, BlockEntityType<PistonMovingBlockEntity> type) {
        return (type == BlockEntityType.PISTON) ? (world1, pos, state1, be) -> {
            boolean shouldTrack = be.getProgress(1) >= 1.0f // current progress, delta=1
                    && !world1.isClientSide; // server side
            if (shouldTrack) {
                if (be instanceof UndoableAccess access) {
                    UndoMixinHelper.pushRecord(access.getUndoId$reden(), () -> "piston block entity tick/" + pos.toShortString());
                }
            }
            PistonMovingBlockEntity.tick(world1, pos, state1, be);
            if (shouldTrack) {
                if (be instanceof UndoableAccess) {
                    UndoMixinHelper.popRecord(() -> "piston block entity tick/" + pos.toShortString());
                }
            }
        } : null;
    }
}
