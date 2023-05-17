package com.github.zly2006.reden.mixin.ctrlz;

import com.github.zly2006.reden.access.ChainedUpdaterView;
import com.github.zly2006.reden.access.PlayerPatchesView;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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

import java.util.HashMap;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinPlayerMode {
    @Shadow @Final protected ServerPlayerEntity player;

    @Shadow protected ServerWorld world;

    @Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private void onDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (world.neighborUpdater instanceof ChainedUpdaterView) {
            if (MalilibSettingsKt.debug()) {
                player.sendMessage(Text.of("Start monitoring of CHAIN - Break block"), false);
            }
            PlayerPatchesView playerView = (PlayerPatchesView) player;
            if (!playerView.isRecording()) {
                playerView.setRecording(true);
                playerView.getBlocks().add(new HashMap<>());
                UpdateMonitorHelper.INSTANCE.getChainFinishListeners().put(world -> {
                    playerView.setRecording(false);
                    return null;
                }, UpdateMonitorHelper.LifeTime.ONCE);
            }
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private void onUseBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (MalilibSettingsKt.debug()) {
            player.sendMessage(Text.of("Start monitoring of CHAIN - Interact block"), false);
        }
        PlayerPatchesView playerView = (PlayerPatchesView) player;
        if (!playerView.isRecording()) {
            playerView.setRecording(true);
            playerView.getBlocks().add(new HashMap<>());
            UpdateMonitorHelper.INSTANCE.getChainFinishListeners().put(it -> {
                playerView.setRecording(false);
                return null;
            }, UpdateMonitorHelper.LifeTime.ONCE);
        }
    }
    @Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private void onUseItemOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (MalilibSettingsKt.debug()) {
            player.sendMessage(Text.of("Start monitoring of CHAIN - Interact block with item"), false);
        }
        PlayerPatchesView playerView = (PlayerPatchesView) player;
        if (!playerView.isRecording()) {
            playerView.setRecording(true);
            playerView.getBlocks().add(new HashMap<>());
            UpdateMonitorHelper.INSTANCE.getChainFinishListeners().put(it -> {
                playerView.setRecording(false);
                return null;
            }, UpdateMonitorHelper.LifeTime.ONCE);
        }
    }
    @Inject(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private void onUseItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (MalilibSettingsKt.debug()) {
            player.sendMessage(Text.of("Start monitoring of CHAIN - Interact item"), false);
        }
        PlayerPatchesView playerView = (PlayerPatchesView) player;
        if (!playerView.isRecording()) {
            playerView.setRecording(true);
            playerView.getBlocks().add(new HashMap<>());
            UpdateMonitorHelper.INSTANCE.getChainFinishListeners().put(it -> {
                playerView.setRecording(false);
                player.damage(player.getDamageSources().cactus(), Float.MAX_VALUE);
                return null;
            }, UpdateMonitorHelper.LifeTime.ONCE);
        }
    }
}
