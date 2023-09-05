package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Explosion.class)
public class MixinExplosion implements UndoableAccess {
    @Shadow @Final private World world;
    @Unique long undoId;

    @Inject(
            method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V",
            at = @At("RETURN")
    )
    private void onInit(World world, Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType, CallbackInfo ci) {
        if (world.isClient) return;
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            DebugKt.debugLogger.invoke("Explosion happened, adding it into record "+ recording.getId());
            undoId = recording.getId();
        }
    }

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void beforeAffectWorld(boolean particles, CallbackInfo ci) {
        if (world.isClient) return;
        if (undoId != 0) {
            UpdateMonitorHelper.pushRecord(undoId, () -> "explosion.blocks");
        }
    }

    @Inject(method = "affectWorld", at = @At("RETURN"))
    private void afterAffectWorld(boolean particles, CallbackInfo ci) {
        if (world.isClient) return;
        if (undoId != 0) {
            UpdateMonitorHelper.popRecord(() -> "explosion.blocks");
        }
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"))
    private void beforeDamageEntities(CallbackInfo ci) {
        if (world.isClient) return;
        if (undoId != 0) {
            UpdateMonitorHelper.pushRecord(undoId, () -> "explosion.entities");
        }
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("RETURN"))
    private void afterDamageEntities(CallbackInfo ci) {
        if (world.isClient) return;
        if (undoId != 0) {
            UpdateMonitorHelper.popRecord(() -> "explosion.entities");
        }
    }

    @Override
    public void setUndoId(long undoId) {
        this.undoId = undoId;
    }

    @Override
    public long getUndoId() {
        return undoId;
    }
}
