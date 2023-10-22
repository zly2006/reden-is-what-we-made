package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommandManager.class)
public class MixinCommands {
    @Redirect(
            method = "execute",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/SharedConstants;isDevelopment:Z"
            )
    )
    private boolean printException() {
        return true;
    }

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
            UpdateMonitorHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.COMMAND);
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
            UpdateMonitorHelper.playerStopRecording(player);
        }
    }
}
