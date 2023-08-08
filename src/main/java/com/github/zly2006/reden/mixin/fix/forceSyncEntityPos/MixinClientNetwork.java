package com.github.zly2006.reden.mixin.fix.forceSyncEntityPos;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientNetwork {
    @Shadow public abstract ClientWorld getWorld();

    @Inject(
            method = "onEntityPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;updateTrackedPosition(DDD)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void onEntityPosition(EntityPositionS2CPacket packet, CallbackInfo ci, Entity entity, double d, double e, double f) {
        if (MalilibSettingsKt.TOGGLE_FORCE_ENTITY_POS_SYNC.getBooleanValue()) {
            entity.setPosition(d, e, f);
            entity.resetPosition();
            if (!entity.isLogicalSideForUpdatingMovement()) {
                float g = (float)(packet.getYaw() * 360) / 256.0f;
                float h = (float)(packet.getPitch() * 360) / 256.0f;
                entity.updateTrackedPositionAndAngles(d, e, f, g, h, 3, true);
                entity.setOnGround(packet.isOnGround());
            }
            ci.cancel();
        }
    }
}
