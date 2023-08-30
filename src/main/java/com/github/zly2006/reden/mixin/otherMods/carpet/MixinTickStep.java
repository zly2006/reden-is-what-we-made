package com.github.zly2006.reden.mixin.otherMods.carpet;

import carpet.commands.TickCommand;
import com.github.zly2006.reden.access.UndoRecordContainerImpl;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TickCommand.class, remap = false)
public class MixinTickStep {
    @Unique private static final UndoRecordContainerImpl recordContainer = new UndoRecordContainerImpl();

    @Inject(method = "step", at = @At("HEAD"))
    private static void beforeStep(ServerCommandSource source, int advance, CallbackInfoReturnable<Integer> cir) {
        recordContainer.setRecording(null);
        ServerPlayerEntity player = source.getPlayer();
        if (player != null) {
            UpdateMonitorHelper.INSTANCE.swap(recordContainer);
            UpdateMonitorHelper.playerStartRecord(player);
        }
    }
}
