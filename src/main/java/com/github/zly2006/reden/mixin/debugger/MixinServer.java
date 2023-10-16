package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.access.WorldData;
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
public abstract class MixinServer implements ServerData.ServerDataAccess {
    @Inject(
            method = "tickWorlds",
            at = @At("HEAD")
    )
    private void beforeTickWorlds(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        getRedenServerData().getTickStage().tick();
    }

    @Inject(
            method = "tickWorlds",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void tickWorld(BooleanSupplier shouldKeepTicking, CallbackInfo ci, Iterator<?> var2, ServerWorld serverWorld) {
        WorldData.Companion.data(serverWorld).tickStage.tick();
    }
}
