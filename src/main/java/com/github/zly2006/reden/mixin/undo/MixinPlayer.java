package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinPlayer {
    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void onAttack(Entity target, CallbackInfo ci) {
        if (RedenCarpetSettings.undoEntities) {
            UpdateMonitorHelper.playerStartRecording(networkHandler.player, PlayerData.UndoRecord.Cause.ATTACK_ENTITY);
        }
    }
    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;attack(Lnet/minecraft/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterAttack(Entity target, CallbackInfo ci) {
        if (RedenCarpetSettings.undoEntities) {
            UpdateMonitorHelper.playerStopRecording(networkHandler.player);
        }
    }
}
