package com.github.zly2006.reden.mixin;

import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Main.class)
public class MixinClientMain {
    @ModifyArgs(
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=username"
                    ),
                    to = @At(
                            value = "CONSTANT",
                            args = "stringValue=uuid"
                    )
            ),
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Ljoptsimple/ArgumentAcceptingOptionSpec;defaultsTo(Ljava/lang/Object;[Ljava/lang/Object;)Ljoptsimple/ArgumentAcceptingOptionSpec;"
            ),
            allow = 1
    )
    private static void changeUsername(Args args) {
        args.set(0, "Dev");
    }
}
