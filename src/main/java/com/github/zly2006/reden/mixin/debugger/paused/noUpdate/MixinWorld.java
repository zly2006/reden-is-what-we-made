package com.github.zly2006.reden.mixin.debugger.paused.noUpdate;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = World.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinWorld {
    @Shadow @Final public boolean isClient;

    @ModifyVariable(
            method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z",
            argsOnly = true,
            ordinal = 0,
            at = @At("HEAD")
    )
    private int cancelNC(int flag) {
        ServerData data = ServerData.getServerData();
        // Disable NC and PP update on both server and client if frozen.
        if (data != null && data.isFrozen()) {
            return (flag | Block.FORCE_STATE) & ~Block.NOTIFY_NEIGHBORS;
        }
        return flag;
    }
}
