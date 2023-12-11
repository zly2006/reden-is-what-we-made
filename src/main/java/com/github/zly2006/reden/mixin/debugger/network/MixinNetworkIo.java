package com.github.zly2006.reden.mixin.debugger.network;


import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.debugger.stages.GlobalNetworkStage;
import com.github.zly2006.reden.debugger.stages.NetworkStage;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import org.apache.http.util.Asserts;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(value = ServerNetworkIo.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinNetworkIo {
    @Shadow
    @Final
    MinecraftServer server;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private List<ClientConnection> connections;

    @Unique
    GlobalNetworkStage stage;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void startTick(CallbackInfo ci) {
        stage = new GlobalNetworkStage(data(server).getTickStage());
        data(server).getTickStageTree().push$reden_is_what_we_made(stage);
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void endTick(CallbackInfo ci) {
        data(server).getTickStageTree().pop$reden_is_what_we_made();
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;tick()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void startConnectionTick(CallbackInfo ci, @Local(ordinal = 0) ClientConnection clientConnection) {
        data(server).getTickStageTree().push$reden_is_what_we_made(new NetworkStage(stage, clientConnection));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void endConnectionTick(CallbackInfo ci, @Local(ordinal = 0) ClientConnection clientConnection) {
        Asserts.check(
                data(server).getTickStageTree().pop$reden_is_what_we_made() instanceof NetworkStage,
                "Popped stage is not a network stage."
        );
    }
}
