package com.github.zly2006.reden.mixin.structureBlock;

import com.github.zly2006.reden.mixinhelper.StructureBlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureBlock.class)
public class MixinStructureBlock {
    @Inject(
            method = "onUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/StructureBlockBlockEntity;openScreen(Lnet/minecraft/entity/player/PlayerEntity;)Z"
            )
    )
    private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient) {
            StructureBlockHelper.INSTANCE.setLastUsed(pos);
            StructureBlockHelper.INSTANCE.setLastUsedWorld(world.getRegistryKey().getValue());
        }
    }
}
