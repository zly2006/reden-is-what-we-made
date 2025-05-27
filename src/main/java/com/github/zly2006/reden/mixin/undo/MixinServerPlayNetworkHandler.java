package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerGamePacketListenerImpl$1")
public abstract class MixinServerPlayNetworkHandler implements ServerboundInteractPacket.Handler {
    @Shadow
    @Final
    ServerGamePacketListenerImpl field_28963;

    @Inject(method = "performInteraction", at = @At(value = "HEAD"))
    public void beforePlayerUseEntity(CallbackInfo ci) {
        if (1 == 1) {
            UpdateMonitorHelper.playerStartRecording(field_28963.player, PlayerData.UndoRecord.Cause.USE_ENTITY);
        }
    }

    @Inject(method = "performInteraction", at = @At(value = "RETURN"))
    public void afterPlayerUseEntity(CallbackInfo info) {
        if (1 == 1) {
            UpdateMonitorHelper.playerStopRecording(field_28963.player);
        }
    }
}
