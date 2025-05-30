package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import com.github.zly2006.reden.utils.DebugKt;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(LevelTicks.class)
@SuppressWarnings("rawtypes")
public class MixinSchedule {
    @Inject(
            method = "runCollectedTicks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private <T> void onRunSchedule(BiConsumer<BlockPos, T> biConsumer, CallbackInfo ci, @Local ScheduledTick scheduledTick) {
        if (1 == 1) {
            long undoId = ((UndoableAccess) scheduledTick).getUndoId$reden();
            UndoMixinHelper.pushRecord(undoId, () -> "scheduled tick/" + scheduledTick.pos().toShortString());
        }
    }
    @Inject(
            method = "runCollectedTicks",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private <T> void afterRunSchedule(BiConsumer<BlockPos, T> biConsumer, CallbackInfo ci, @Local ScheduledTick scheduledTick) {
        if (1 == 1) {
            UndoMixinHelper.popRecord(() -> "scheduled tick/" + scheduledTick.pos().toShortString());
        }
    }
    @Inject(
            method = "schedule",
            at = @At(
                    value = "HEAD"
            )
    )
    private <T> void onAddSchedule(ScheduledTick<T> scheduledTick, CallbackInfo ci) {
        PlayerData.UndoRecord recording = UndoMixinHelper.INSTANCE.getRecording();
        if (recording != null) {
            DebugKt.debugLogger.invoke("Scheduled tick at " + scheduledTick.pos() + ", adding it into record " + recording.getId());
            // inherit parent id
            ((UndoableAccess) scheduledTick).setUndoId$reden(recording.getId());
        }
    }
}
