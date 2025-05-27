package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public class MixinExplosion implements UndoableAccess {
    @Shadow @Final private Level level;
    @Unique long undoId;

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Explosion$BlockInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)V",
            at = @At("RETURN")
    )
    private void onInit(CallbackInfo ci) {
        if (level.isClientSide) return;
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            DebugKt.debugLogger.invoke("Explosion happened, adding it into record "+ recording.getId());
            undoId = recording.getId();
        }
    }

    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    private void beforeAffectWorld(boolean particles, CallbackInfo ci) {
        if (level.isClientSide) return;
        UpdateMonitorHelper.pushRecord(undoId, () -> "explosion.blocks");
    }

    @Inject(method = "finalizeExplosion", at = @At("RETURN"))
    private void afterAffectWorld(boolean particles, CallbackInfo ci) {
        if (level.isClientSide) return;
        UpdateMonitorHelper.popRecord(() -> "explosion.blocks");
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void beforeDamageEntities(CallbackInfo ci) {
        if (level.isClientSide) return;
        UpdateMonitorHelper.pushRecord(undoId, () -> "explosion.entities");
    }

    @Inject(method = "explode", at = @At("RETURN"))
    private void afterDamageEntities(CallbackInfo ci) {
        if (level.isClientSide) return;
        UpdateMonitorHelper.popRecord(() -> "explosion.entities");
    }

    @Override
    public void setUndoId$reden(long undoId) {
        this.undoId = undoId;
    }

    @Override
    public long getUndoId$reden() {
        return undoId;
    }
}
