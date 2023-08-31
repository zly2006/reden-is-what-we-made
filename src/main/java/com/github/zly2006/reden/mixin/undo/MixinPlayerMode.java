package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.ChainedUpdaterView;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinPlayerMode {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    protected ServerWorld world;

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private void onDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world.neighborUpdater instanceof ChainedUpdaterView) {
            DebugKt.debugLogger.invoke("Start monitoring of CHAIN - Break block");
            UpdateMonitorHelper.playerStartRecord(player);
        }
    }

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private void afterDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        DebugKt.debugLogger.invoke("Stop monitoring of CHAIN - Break block");
        UpdateMonitorHelper.playerStartRecord(player);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private void onUseBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        DebugKt.debugLogger.invoke("Start monitoring of CHAIN - Interact block");
        UpdateMonitorHelper.playerStartRecord(player);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private void afterUseBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        DebugKt.debugLogger.invoke("Stop monitoring of CHAIN - Interact block");
        UpdateMonitorHelper.playerStartRecord(player);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private void onUseItemOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        DebugKt.debugLogger.invoke("Start monitoring of CHAIN - Interact block with item");
        UpdateMonitorHelper.playerStartRecord(player);
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private void afterUseItemOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        DebugKt.debugLogger.invoke("Stop monitoring of CHAIN - Interact block with item");
        UpdateMonitorHelper.playerStartRecord(player);
    }

    @Inject(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private void onUseItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        DebugKt.debugLogger.invoke("Start monitoring of CHAIN - Interact item");
        UpdateMonitorHelper.playerStartRecord(player);
    }

    @Inject(method = "interactItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private void afterUseItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        DebugKt.debugLogger.invoke("Stop monitoring of CHAIN - Interact item");
        UpdateMonitorHelper.playerStartRecord(player);
    }
}
