package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.BlockEntityInterface;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements BlockEntityInterface {
    @Shadow @Nullable protected Level level;
    @Final @Shadow protected BlockPos worldPosition;
    @Shadow private BlockState blockState;

    @Shadow public abstract CompoundTag saveWithId(HolderLookup.Provider provider);

    @Unique CompoundTag lastSavedNbt = null;

    @Override
    public void saveLastNbt$reden() {
        if (level != null && !level.isClientSide) {
            DebugKt.debugLogger.invoke("before saving lastNBT at " + worldPosition.toShortString() + ", data=" + lastSavedNbt);
            lastSavedNbt = this.saveWithId(level.registryAccess()).copy();
            DebugKt.debugLogger.invoke("saved lastNBT at " + worldPosition.toShortString() + ", cause=manual, " + lastSavedNbt);
        }
    }

    @Override
    @Nullable
    public CompoundTag getLastSavedNbt$reden() {
        return lastSavedNbt;
    }

    @Inject(
            method = "setChanged()V",
            at = @At("HEAD")
    )
    private void onBlockEntityChanged(CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            UpdateMonitorHelper.postSetBlock(serverLevel, worldPosition, blockState, true);
        }
    }

    @Inject(
            method = "loadWithComponents",
            at = @At("TAIL")
    )
    private void onReadNbt(CompoundTag nbt, HolderLookup.Provider registryLookup, CallbackInfo ci) {
        DebugKt.debugLogger.invoke("before saving lastNBT at " + worldPosition.toShortString() + ", data=" + lastSavedNbt);
        if (lastSavedNbt == null) {
            lastSavedNbt = nbt.copy();
            DebugKt.debugLogger.invoke("saved lastNBT at " + worldPosition.toShortString() + ", cause=read, " + lastSavedNbt);
        } else {
            DebugKt.debugLogger.invoke("skip saving lastNBT at " + worldPosition.toShortString());
        }
    }
}
