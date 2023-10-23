package com.github.zly2006.reden.mixin.debugger;


import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.stages.NetworkStage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(ServerNetworkIo.class)
public class MixinNetworkIo {
    @Shadow @Final private MinecraftServer server;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/ClientConnection;tick()V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void prepareNU(CallbackInfo ci, List<?> var1, Iterator<?> iterator, ClientConnection clientConnection) {
        // Note: here we modify the stage tree directly because it is NU.
        //     Here is used to provide world access to the stage tree.
        //     Otherwise, we should set the children in tick method, and let the server root tick them.
        ServerData data = ServerData.data(server);
        var stage = new NetworkStage(data.getTickStage(), clientConnection);
        data.getTickStageTree().insert2child(stage);
        data.getTickStageTree().next().tick();
    }
}
