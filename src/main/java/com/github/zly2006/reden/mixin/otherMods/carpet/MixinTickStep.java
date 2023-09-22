package com.github.zly2006.reden.mixin.otherMods.carpet;

import carpet.commands.TickCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TickCommand.class, remap = false)
public class MixinTickStep {
    @Inject(method = "step", at = @At("HEAD"))
    private static void beforeStep(ServerCommandSource source, int advance, CallbackInfoReturnable<Integer> cir) {
        // todo: set stepping tick start
        //
    }
}
