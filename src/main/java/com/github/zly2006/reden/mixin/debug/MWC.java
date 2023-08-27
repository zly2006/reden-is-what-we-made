package com.github.zly2006.reden.mixin.debug;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public class MWC {
    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void on(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        if (pos.equals(new BlockPos(5, -57, -4))) {
            System.out.println("aaaaaaaaa");
        }
    }
}
