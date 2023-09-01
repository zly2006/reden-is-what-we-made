package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public class MixinCommands {
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I",
                    shift = At.Shift.BEFORE,
                    remap = false
            )
    )
    private void onExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayerEntity player) {
            DebugKt.debugLogger.invoke("Start monitoring of CHAIN - Command");
            UpdateMonitorHelper.playerStartRecording(player);
        }
    }

    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void afterExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayerEntity player) {
            DebugKt.debugLogger.invoke("Stop monitoring of CHAIN - Command");
            UpdateMonitorHelper.playerStopRecording(player);
        }
    }
}
