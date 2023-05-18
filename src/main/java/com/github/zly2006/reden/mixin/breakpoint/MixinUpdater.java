package com.github.zly2006.reden.mixin.breakpoint;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.event.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NeighborUpdater.class)
public interface MixinUpdater {
    @Inject(method = "replaceWithStateForNeighborUpdate(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;II)V", at = @At(value = "HEAD"))
    private static void inject(WorldAccess world, Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth, CallbackInfo ci) {
        world.emitGameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
    }
}
