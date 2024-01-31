package com.github.zly2006.reden.mixin.rvc;

import com.github.zly2006.reden.rvc.gui.SelectionListScreenKt;
import com.github.zly2006.reden.rvc.tracking.WorldInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk {
    @Shadow @Final private World world;

    @Shadow public abstract World getWorld();

    @Inject(
            method = "setBlockState",
            at = @At("TAIL")
    )
    private void onBlockChanged(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> cir) {
        //todo
        if (world.isClient) {
            var repo = SelectionListScreenKt.getSelectedRepository();
            if (repo == null) return;
            if (repo.getPlacementInfo() == null) return;
            WorldInfo worldInfo = WorldInfo.Companion.getWorldInfo(MinecraftClient.getInstance());
            if (!worldInfo.equals(repo.getPlacementInfo().getWorldInfo())) return;

            var structure = repo.head();
            if (structure.isInArea(structure.getRelativeCoordinate(pos))) {
                if (state.isAir()) {
                    structure.onBlockRemoved(pos);
                } else {
                    structure.onBlockAdded(pos);
                }
            } else if (Arrays.stream(Direction.values()).anyMatch(dir -> structure.isInArea(structure.getRelativeCoordinate(pos.offset(dir))))) {
                structure.onBlockAdded(pos);
            }
        }
    }
}
