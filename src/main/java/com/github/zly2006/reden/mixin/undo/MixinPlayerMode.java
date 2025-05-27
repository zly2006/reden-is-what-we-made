package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MixinPlayerMode {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    protected ServerLevel level;

    // Inject before onBreak
    // Because tall or wide blocks such as doors or beds override [onBreak] to break the other part.
    // (Along with AbstractBlock.getStateForNeighborUpdate.)
    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private void onDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.BREAK_BLOCK);
    }

    // Inject after onBroken
    @Inject(
        method = "destroyBlock",
        at = @At(
            value = "INVOKE",
            //? if < 1.21.5 {
            /*target = "Lnet/minecraft/server/level/ServerPlayerGameMode;isCreative()Z"
            *///?} else {
            target = "Lnet/minecraft/server/level/ServerPlayer;preventsBlockDrops()Z"
            //?}
        )
    )
    private void afterDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        UpdateMonitorHelper.playerStopRecording(player);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void onUseBlock(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.USE_BLOCK);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void afterUseBlock(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        UpdateMonitorHelper.playerStopRecording(player);
    }

    //? if <= 1.21.1 {
    /*@Inject(method = "useItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"))
    *///?} else {
    @Inject(method = "useItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    //?}
    private void onUseItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.USE_ITEM);
    }
    //? if <= 1.21.1 {
    /*@Inject(method = "useItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/item/ItemStack;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"))
    *///?} else {
    @Inject(method = "useItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/item/ItemStack;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
    //?}
    private void afterUseItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        UpdateMonitorHelper.playerStopRecording(player);
    }
}
