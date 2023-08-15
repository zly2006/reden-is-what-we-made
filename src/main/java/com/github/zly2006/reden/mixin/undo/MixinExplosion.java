package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(Explosion.class)
public class MixinExplosion implements UndoableAccess {
    @Unique
    long undoId;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    private void beforeAffectWorld(boolean particles, CallbackInfo ci) {
        if (undoId != 0) {
            UpdateMonitorHelper.INSTANCE.setRecording(UpdateMonitorHelper.INSTANCE.getUndoRecordsMap().get(undoId));
        }
    }

    @Inject(method = "affectWorld", at = @At("TAIL"))
    private void afterAffectWorld(boolean particles, CallbackInfo ci) {
        if (undoId != 0) {
            UpdateMonitorHelper.INSTANCE.setRecording(null);
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
