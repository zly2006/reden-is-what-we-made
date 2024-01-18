package com.github.zly2006.reden.mixin.rvc;

import com.github.zly2006.reden.access.ClientData;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.zly2006.reden.access.ClientData.getData;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk {
    @Shadow @Final private World world;

    @Inject(
            method = "setBlockState",
            at = @At("HEAD")
    )
    private void onBlockChange(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        //todo
        if (world.isClient) {
            ClientData data = getData(MinecraftClient.getInstance());
            data.getRvcStructures().values().forEach(repo -> {
                var structure = repo.head();
                if (structure.isInArea(structure.getRelativeCoordinate(pos))) {
                    if (state.isAir()) {
                        structure.onBlockRemoved(pos);
                    } else {
                        structure.onBlockAdded(pos);
                    }
                }
            });
        }
    }
}
