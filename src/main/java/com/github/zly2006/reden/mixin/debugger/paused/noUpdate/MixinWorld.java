package com.github.zly2006.reden.mixin.debugger.paused.noUpdate;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(World.class)
public class MixinWorld {
    @ModifyVariable(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            argsOnly = true,
            ordinal = 0,
            at = @At("HEAD")
    )
    private int cancelNC(int flag) {
        return flag & ~Block.NOTIFY_NEIGHBORS;
    }
}
