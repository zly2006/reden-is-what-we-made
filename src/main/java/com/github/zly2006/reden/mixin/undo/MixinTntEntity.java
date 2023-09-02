package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(TntEntity.class)
public abstract class MixinTntEntity extends Entity implements UndoableAccess {
    @Unique long undoId;

    public MixinTntEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V",
            at = @At("RETURN")
    )
    private void onInit(EntityType<?> entityType, World world, CallbackInfo ci) {
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            DebugKt.debugLogger.invoke("TNT spawned, adding it into record "+ recording.getId());
            undoId = recording.getId();
        }
    }

    @Inject(method = "explode", at = @At("HEAD"))
    private void beforeExplode(CallbackInfo ci) {
        DebugKt.debugLogger.invoke("TNT explode start, undoId=" + undoId);
        UpdateMonitorHelper.pushRecord(undoId, "tnt explode/" + getId());
    }

    @Inject(method = "explode", at = @At("TAIL"))
    private void afterExplode(CallbackInfo ci) {
        DebugKt.debugLogger.invoke("TNT explode end, undoId=" + undoId);
        UpdateMonitorHelper.popRecord("tnt explode/" + getId());
    }

    @Override
    public long getUndoId() {
        return undoId;
    }

    @Override
    public void setUndoId(long undoId) {
        this.undoId = undoId;
    }
}
