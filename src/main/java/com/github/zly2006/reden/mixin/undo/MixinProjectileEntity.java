package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileEntity.class)
public abstract class MixinProjectileEntity extends Entity implements UndoableAccess, Ownable {
    public MixinProjectileEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "onCollision",
            at = @At("HEAD")
    )
    private void beforeHit(HitResult hitResult, CallbackInfo ci) {
        if (this.getOwner() instanceof ServerPlayerEntity player) {
            UpdateMonitorHelper.pushRecord(getUndoId$reden(), () -> "projectile hit/" + getId());
        }
    }

    @Inject(
            method = "onCollision",
            at = @At("RETURN")
    )
    private void afterHit(HitResult hitResult, CallbackInfo ci) {
        if (this.getOwner() instanceof ServerPlayerEntity player) {
            UpdateMonitorHelper.popRecord(() -> "projectile hit/" + getId());
        }
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initUndoId(EntityType<?> entityType, World world, CallbackInfo ci) {
        if (!world.isClient) {
            PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
            if (recording != null) {
                DebugKt.debugLogger.invoke("Projectile spawned, adding it into record " + recording.getId());
                setUndoId$reden(recording.getId());
            }
        }
    }
}
