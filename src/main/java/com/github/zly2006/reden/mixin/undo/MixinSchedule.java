package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.ScheduledTickAccess;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
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
public class MixinSchedule {
    @SuppressWarnings("rawtypes")
    @Inject(
            method = "tick(Ljava/util/function/BiConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private <T> void onRunSchedule(BiConsumer<BlockPos, T> ticker, CallbackInfo ci, OrderedTick orderedTick) {
        long undoId = ((ScheduledTickAccess) orderedTick).getUndoId();
        PlayerData.UndoRecord record = UpdateMonitorHelper.INSTANCE.getUndoRecordsMap().get(undoId);
        if (MalilibSettingsKt.DEBUG_LOGGER.getBooleanValue() && record != null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Scheduled tick at " + orderedTick.pos() + ", adding it into record " + undoId));
        }
        UpdateMonitorHelper.INSTANCE.setRecording(record);
    }
    @Inject(
            method = "tick(Ljava/util/function/BiConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void afterRunSchedule(CallbackInfo ci) {
        UpdateMonitorHelper.INSTANCE.setRecording(null);
    }
    @Inject(
            method = "scheduleTick",
            at = @At(
                    value = "HEAD"
            )
    )
    private <T> void onAddSchedule(OrderedTick<T> orderedTick, CallbackInfo ci) {
        ScheduledTickAccess access = (ScheduledTickAccess) orderedTick;
        PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
        if (recording != null) {
            // inherit parent id
            access.setUndoId(recording.getId());
        }
    }
}
