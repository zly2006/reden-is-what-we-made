package com.github.zly2006.reden.mixin.pearl;

import com.github.zly2006.reden.network.TntSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;

@Mixin(value = Explosion.class, priority = 100)
public class MixinExplosion {
    @Shadow @Final private float power;

    @SuppressWarnings({"InvalidInjectorMethodSignature", "rawtypes"})
    @Inject(
            method = "collectBlocksAndDamageEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion()Z",
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onExplode(CallbackInfo ci, Set set, int a, float q, int k, int l, int r, int s, int t, int u, List list, Vec3d vec3d, int v, Entity entity) {
        if (entity instanceof SnowballEntity snowball && !TntSyncPacket.Companion.getSyncedTntPos().contains(vec3d)) {
            if (snowball.getOwner() instanceof ServerPlayerEntity player) {
                ServerPlayNetworking.send(player, new TntSyncPacket(
                        snowball.getUuid(),
                        snowball.getPos(),
                        snowball.getVelocity(),
                        this.power,
                        vec3d
                ));
                TntSyncPacket.Companion.getSyncedTntPos().add(vec3d);
            }
        }
    }
}
