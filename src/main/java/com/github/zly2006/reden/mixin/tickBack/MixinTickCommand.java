package com.github.zly2006.reden.mixin.tickBack;

import carpet.commands.TickCommand;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TickCommand.class, remap = false)
public class MixinTickCommand {
    @Redirect(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/brigadier/arguments/IntegerArgumentType;integer(II)Lcom/mojang/brigadier/arguments/IntegerArgumentType;"
            )
    )
    private static IntegerArgumentType onIntegerArg(int min, int max) {
        if (min == 1 && max == 72000) {
            return IntegerArgumentType.integer(-RedenCarpetSettings.tickBackMaxTicks, max);
        } else {
            return IntegerArgumentType.integer(min, max);
        }
    }
}
