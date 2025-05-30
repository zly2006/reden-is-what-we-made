package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SynchedEntityData.class)
public class MixinDataTracker {
    @Shadow
    @Final
    private SyncedDataHolder entity;

    @Inject(
            method = "set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;Z)V",
            at = @At("HEAD")
    )
    private <T> void beforeDataSet(EntityDataAccessor<T> entityDataAccessor, T object, boolean bl, CallbackInfo ci) {
        if (entity instanceof Entity modifiedEntity) {
            if (modifiedEntity.level().isClientSide()) return;
            UndoMixinHelper.tryAddRelatedEntity(modifiedEntity);
        }
    }
}
