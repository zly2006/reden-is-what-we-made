package com.github.zly2006.reden.mixin.debug.compatTest;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServet_ct {
    @Inject(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void onTickWorld(BooleanSupplier shouldKeepTicking, CallbackInfo ci, Iterator<?> var2, ServerWorld serverWorld) {
        serverWorld.getDebugString();
    }
}
