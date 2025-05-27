package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "stopUsingItem()V",
            at = @At("HEAD")
    )
    private void stopUsingItem(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer player) {
            UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.USE_ITEM);
        }
    }

    @Inject(
            method = "stopUsingItem()V",
            at = @At("RETURN")
    )
    private void afterStopUsingItem(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer player) {
            UpdateMonitorHelper.playerStopRecording(player);
        }
    }

    @Inject(
            method = "die",
            at = @At("HEAD")
    )
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        UpdateMonitorHelper.tryAddRelatedEntity(this);
    }
}
