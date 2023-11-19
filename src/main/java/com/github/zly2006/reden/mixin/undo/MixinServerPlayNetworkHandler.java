package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$1")
public abstract class MixinServerPlayNetworkHandler implements PlayerInteractEntityC2SPacket.Handler {
    @Shadow
    @Final
    ServerPlayNetworkHandler field_28963;

    @Inject(method = "interact(Lnet/minecraft/util/Hand;)V", at = @At(value = "HEAD"))
    public void beforePlayerUseEntity(Hand hand, CallbackInfo info) {
        ServerPlayerEntity player = field_28963.player;
        UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.USE_ENTITY);
    }

    @Inject(method = "interact(Lnet/minecraft/util/Hand;)V", at = @At(value = "RETURN"))
    public void afterPlayerUseEntity(Hand hand, CallbackInfo info) {
        ServerPlayerEntity player = field_28963.player;
        UpdateMonitorHelper.playerStopRecording(player);
    }
}
