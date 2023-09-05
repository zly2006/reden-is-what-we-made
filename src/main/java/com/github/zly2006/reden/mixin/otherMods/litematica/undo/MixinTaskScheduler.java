package com.github.zly2006.reden.mixin.otherMods.litematica.undo;

import com.github.zly2006.reden.ModNames;
import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import fi.dy.masa.litematica.scheduler.TaskScheduler;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Restriction(
     require = @Condition(ModNames.litematica)
)
@Mixin(TaskScheduler.class)
// client-side only
public class MixinTaskScheduler {
    @Inject(
            method = "runTasks",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lfi/dy/masa/litematica/scheduler/ITask;execute()Z",
                    remap = false
            ),
            remap = false
    )
    private void beforeRunLitematicaTask(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        IntegratedServer server = mc.getServer();
        if (mc.isIntegratedServerRunning() && server != null && MalilibSettingsKt.UNDO_SUPPORT_LITEMATICA_OPERATION.getBooleanValue()) {
            //noinspection DataFlowIssue
            UpdateMonitorHelper.playerStartRecording(server.getPlayerManager().getPlayer(server.localPlayerUuid), PlayerData.UndoRecord.Cause.LITEMATICA_TASK);
        }
    }
    @Inject(
            method = "runTasks",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lfi/dy/masa/litematica/scheduler/ITask;execute()Z",
                    remap = false
            ),
            remap = false
    )
    private void afterRunLitematicaTask(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        IntegratedServer server = mc.getServer();
        if (mc.isIntegratedServerRunning() && server != null && MalilibSettingsKt.UNDO_SUPPORT_LITEMATICA_OPERATION.getBooleanValue()) {
            //noinspection DataFlowIssue
            UpdateMonitorHelper.playerStopRecording(server.getPlayerManager().getPlayer(server.localPlayerUuid));
        }
    }
}
