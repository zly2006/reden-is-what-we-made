package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.BlockEntityInterface;
import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
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
    @Shadow private DataComponentMap components;
    //? if < 1.21.6 {
    /*@Shadow public abstract CompoundTag saveWithId(HolderLookup.Provider provider);
    *///?} else {
    @Shadow public abstract void saveWithId(net.minecraft.world.level.storage.ValueOutput par1);
    //?}

    @Unique CompoundTag lastSavedNbt = null;
    @Unique DataComponentMap lastComponents = null;

    @Override
    public void saveLastNbt$reden() {
        if (level != null && !level.isClientSide) {
            DebugKt.debugLogger.invoke("before saving lastNBT at " + worldPosition.toShortString() + ", nbt=" + lastSavedNbt + ", components=" + components);
            if (isComponentsValid(components)) {
                lastComponents = components;
                DebugKt.debugLogger.invoke("saved lastComponents at " + worldPosition.toShortString() + ", cause=reden manually, " + lastComponents);
            } else {
                //? if < 1.21.6 {
                /*lastSavedNbt = this.saveWithId(level.registryAccess());
                DebugKt.debugLogger.invoke("saved lastNBT at " + worldPosition.toShortString() + ", cause=reden manually, " + lastSavedNbt);
                *///?}
            }
        }
    }

    @Unique
    private boolean isComponentsValid(DataComponentMap lastComponents) {
        return false; // only has block state and block entity data, which are not useful for undo
    }

    @Override
    public @Nullable Object getLastSavedNbt$reden() {
        if (isComponentsValid(lastComponents)) {
            DebugKt.debugLogger.invoke("getLastSavedNbt at " + worldPosition.toShortString() + ", using lastComponents=" + lastComponents);
            return lastComponents;
        } else if (lastSavedNbt != null) {
            DebugKt.debugLogger.invoke("getLastSavedNbt at " + worldPosition.toShortString() + ", using lastSavedNbt=" + lastSavedNbt);
            return lastSavedNbt;
        } else {
            DebugKt.debugLogger.invoke("getLastSavedNbt at " + worldPosition.toShortString() + ", no saved data");
            return null;
        }
    }

    @Inject(
            method = "setChanged()V",
            at = @At("HEAD")
    )
    private void onBlockEntityChanged(CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            UndoMixinHelper.postSetBlock(serverLevel, worldPosition, blockState, true);
        }
    }

    // Only for initialization, do not call more than once
    @Inject(
            method = "loadWithComponents",
            at = @At("TAIL")
    )
    private void onReadNbt(CallbackInfo ci) {
        DebugKt.debugLogger.invoke("init: before saving lastNBT at " + worldPosition.toShortString() + ", data=" + lastSavedNbt);
        if (lastSavedNbt == null && lastComponents == null) {
            if (isComponentsValid(components)) {
                lastComponents = components;
                DebugKt.debugLogger.invoke("init: saved lastComponents at " + worldPosition.toShortString() + ", cause=reden init, " + lastComponents);
            } else if (level != null) {
                //? if < 1.21.6 {
                /*lastSavedNbt = this.saveWithId(level.registryAccess());
                DebugKt.debugLogger.invoke("init: saved lastNBT at " + worldPosition.toShortString() + ", cause=reden init, " + lastSavedNbt);
                *///?}
            }
        } else {
            DebugKt.debugLogger.invoke("init: skip saving lastNBT at " + worldPosition.toShortString());
        }
    }
}
