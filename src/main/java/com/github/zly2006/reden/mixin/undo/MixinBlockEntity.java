package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.BlockEntityInterface;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    @Shadow @Nullable protected World world;

    @Shadow public abstract NbtCompound createNbtWithIdentifyingData();

    @Shadow private BlockState cachedState;
    @Shadow @Final protected BlockPos pos;
    @Unique NbtCompound lastSavedNbt = null;

    @Override
    public void saveLastNbt$reden() {
        if (world != null && !world.isClient && RedenCarpetSettings.Options.undoBlockEntities) {
            DebugKt.debugLogger.invoke("before saving lastNBT at " + pos.toShortString() + ", data=" + lastSavedNbt);
            lastSavedNbt = this.createNbtWithIdentifyingData().copy();
            DebugKt.debugLogger.invoke("saved lastNBT at " + pos.toShortString() + ", cause=manual, " + lastSavedNbt);
        }
    }

    @Override
    @Nullable
    public NbtCompound getLastSavedNbt$reden() {
        return lastSavedNbt;
    }

    @Inject(
            method = "markDirty()V",
            at = @At("HEAD")
    )
    private void onBlockEntityChanged(CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld && RedenCarpetSettings.Options.undoBlockEntities) {
            UpdateMonitorHelper.postSetBlock(serverWorld, pos, cachedState, true);
        }
    }

    @Inject(
            method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At("TAIL")
    )
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        DebugKt.debugLogger.invoke("before saving lastNBT at " + pos.toShortString() + ", data=" + lastSavedNbt);
        if (lastSavedNbt == null && RedenCarpetSettings.Options.undoBlockEntities) {
            lastSavedNbt = nbt.copy();
            DebugKt.debugLogger.invoke("saved lastNBT at " + pos.toShortString() + ", cause=read, " + lastSavedNbt);
        } else {
            DebugKt.debugLogger.invoke("skip saving lastNBT at " + pos.toShortString());
        }
    }
}
