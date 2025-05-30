package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class MixinCommands {
    @WrapOperation(
            method = "performCommand",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"
            )
    )
    private boolean printException(Operation<Boolean> original) {
        return true;
    }

    @Inject(
            method = "performCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void onExecute(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayer player) {
            UndoMixinHelper.playerStartRecording(player, PlayerData.UndoRecord.Cause.COMMAND);
        }
    }

    @Inject(
            method = "performCommand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterExecute(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayer player) {
            UndoMixinHelper.playerStopRecording(player);
        }
    }
}
