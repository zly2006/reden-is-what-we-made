package com.github.zly2006.reden.mixin.hopperDelaySync;

import com.github.zly2006.reden.network.HopperCDSync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class MixinHoppers extends LootableContainerBlockEntity {
    @Shadow private int transferCooldown;

    protected MixinHoppers(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = "serverTick",
            at = @At("RETURN")
    )
    private static void onCD(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        if (world.isClient) {
            return;
        }
        ((ServerWorld) world).getPlayers().forEach(player -> {
            if (player.currentScreenHandler instanceof HopperScreenHandler sh) {
                if (sh.inventory == blockEntity) {
                    player.server.execute(() -> {
                        ServerPlayNetworking.send(player, new HopperCDSync(pos, blockEntity.transferCooldown));
                    });
                }
            }
        });
    }

    @Inject(
            method = "createScreenHandler",
            at = @At("RETURN")
    )
    private void updateCD(int syncId, PlayerInventory playerInventory, CallbackInfoReturnable<ScreenHandler> cir) {
        if (playerInventory.player instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, new HopperCDSync(this.getPos(), transferCooldown));
        }
    }
}
