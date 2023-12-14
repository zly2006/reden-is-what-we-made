package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningEntity.class)
public abstract class MixinLightningEntity extends Entity implements UndoableAccess {
    public MixinLightningEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initUndoId(EntityType<?> entityType, World world, CallbackInfo ci) {
        if (!world.isClient) {
            PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
            if (recording != null) {
                DebugKt.debugLogger.invoke("Lightning spawned, adding it into record " + recording.getId());
                setUndoId$reden(recording.getId());
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void beforeTick(CallbackInfo ci) {
        if (getWorld().isClient) return;
        UpdateMonitorHelper.pushRecord(getUndoId$reden(), () -> "lightning tick/" + getId());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void afterTick(CallbackInfo ci) {
        if (getWorld().isClient) return;
        UpdateMonitorHelper.popRecord(() -> "lightning tick/" + getId());
    }
}
