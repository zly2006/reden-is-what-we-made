package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.mojang.brigadier.ParseResults;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;

@Mixin(CommandManager.class)
public class MixinCommands {
    @Inject(method = "execute", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I"))
    private void onExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayerEntity player) {
            if (MalilibSettingsKt.debug()) {
                player.sendMessage(Text.of("Start monitoring of CHAIN - Command"), false);
            }
            PlayerData playerView = PlayerData.Companion.data(player);
            if (!playerView.isRecording()) {
                playerView.setRecording(true);
                playerView.getUndo().add(new HashMap<>());
            }
        }
    }

    @Inject(method = "execute", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I"))
    private void afterExecute(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayerEntity player) {
            if (MalilibSettingsKt.debug()) {
                player.sendMessage(Text.of("Stop monitoring of CHAIN - Command"), false);
            }
            PlayerData playerView = PlayerData.Companion.data(player);
            if (playerView.isRecording()) {
                playerView.stopRecording(player.world);
            }
        }
    }
}
