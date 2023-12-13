package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "stopUsingItem()V",
            at = @At("HEAD")
    )
    private void stopUsingItem(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayerEntity player) {
            UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.USE_ITEM);
        }
    }

    @Inject(
            method = "stopUsingItem()V",
            at = @At("RETURN")
    )
    private void afterStopUsingItem(CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayerEntity player) {
            UpdateMonitorHelper.playerStopRecording(player);
        }
    }

    @Inject(
            method = "onDeath",
            at = @At("HEAD")
    )
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        UpdateMonitorHelper.tryAddRelatedEntity(this);
    }
}
