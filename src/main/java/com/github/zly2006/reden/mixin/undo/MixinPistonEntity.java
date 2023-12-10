package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.block.entity.PistonBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public class MixinPistonEntity implements UndoableAccess {
    @Unique
    long undoId;

    @Override
    public long getUndoId$reden() {
        return undoId;
    }

    @Override
    public void setUndoId$reden(long undoId) {
        this.undoId = undoId;
    }

    @Inject(method = "<init>(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            undoId = recording.getId();
        }
    }

    @Inject(method = "finish", at = @At("HEAD"))
    private void beforeFinish(CallbackInfo ci) {
        if (undoId != 0) {
            DebugKt.debugLogger.invoke("---Piston finishing, setting it to record "+ undoId);
            //UpdateMonitorHelper.INSTANCE.setRecording(UpdateMonitorHelper.INSTANCE.getUndoRecordsMap().get(undoId));
        }
    }

    @Inject(method = "finish", at = @At("RETURN"))
    private void afterFinish(CallbackInfo ci) {
        if (undoId != 0) {
            DebugKt.debugLogger.invoke("---Piston finished, removing it from record "+ undoId);
            //UpdateMonitorHelper.INSTANCE.setRecording(null);
        }
    }
}
