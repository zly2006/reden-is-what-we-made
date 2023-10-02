package com.redenmc.mr120.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinServer {
    @Redirect(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;tick(Ljava/util/function/BooleanSupplier;)V"
            )
    )
    private void wrapperTick(MinecraftServer server, BooleanSupplier shouldKeepTicking) {
        server.tick(shouldKeepTicking);
    }
}
