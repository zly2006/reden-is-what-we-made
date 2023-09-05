package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
@SuppressWarnings("rawtypes")
public class MixinSchedule {
    @Inject(
            method = "tick(Ljava/util/function/BiConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private <T> void onRunSchedule(BiConsumer<BlockPos, T> ticker, CallbackInfo ci, OrderedTick orderedTick) {
        long undoId = ((UndoableAccess) orderedTick).getUndoId();
        UpdateMonitorHelper.pushRecord(undoId, () -> "scheduled tick/" + orderedTick.pos().toShortString());
    }
    @Inject(
            method = "tick(Ljava/util/function/BiConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void afterRunSchedule(BiConsumer<BlockPos, ?> ticker, CallbackInfo ci, OrderedTick orderedTick) {
        UpdateMonitorHelper.popRecord(() -> "scheduled tick/" + orderedTick.pos().toShortString());
    }
    @Inject(
            method = "scheduleTick",
            at = @At(
                    value = "HEAD"
            )
    )
    private <T> void onAddSchedule(OrderedTick<T> orderedTick, CallbackInfo ci) {
        UndoableAccess access = (UndoableAccess) orderedTick;
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            DebugKt.debugLogger.invoke("Scheduled tick at " + orderedTick.pos() + ", adding it into record " + recording.getId());
            // inherit parent id
            access.setUndoId(recording.getId());
        }
    }
}
