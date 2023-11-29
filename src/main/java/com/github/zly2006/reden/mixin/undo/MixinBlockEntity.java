package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.BlockEntityInterface;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements BlockEntityInterface {
    @Unique NbtCompound lastSavedNbt = null;

    @Override
    public NbtCompound getLastSavedNbt() {
        return lastSavedNbt;
    }

    @Inject(
            method = "markDirty(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
            at = @At("HEAD")
    )
    private static void onBlockEntityChanged(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (world instanceof ServerWorld serverWorld) {
            UpdateMonitorHelper.monitorSetBlock(serverWorld, pos, null);
        }
    }

    @Inject(
            method = "readNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At("TAIL")
    )
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        lastSavedNbt = nbt;
    }

    @Inject(
            method = "writeNbt",
            at = @At("TAIL")
    )
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        lastSavedNbt = nbt;
    }
}
