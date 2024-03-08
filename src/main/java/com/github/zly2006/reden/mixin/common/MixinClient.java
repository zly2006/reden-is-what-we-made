package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.ClientData;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.rvc.tracking.RvcRepository;
import com.github.zly2006.reden.task.TaskKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinClient implements ClientData.ClientDataAccess, ServerData.ClientSideServerDataAccess {
    @Shadow @Nullable public ClientPlayerEntity player;
    @Unique ClientData clientData = new ClientData((MinecraftClient) (Object) this);
    @Unique ServerData serverData = null;

    @NotNull
    @Override
    public ClientData getClientData$reden() {
        return clientData;
    }

    @Override
    public ServerData getServerData$reden() {
        return serverData;
    }

    @Override
    public void setServerData$reden(@Nullable ServerData serverData) {
        this.serverData = serverData;
    }

    @Inject(
            method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V",
            at = @At(
                    value = "FIELD",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;"
            )
    )
    private void resetServerDataOnDisconnect(Screen screen, CallbackInfo ci) {
        if (player == null) {
            serverData = null;
            clientData.getBreakpoints().clear();
        }
    }

    @Inject(
            method = "setWorld",
            at = @At("HEAD")
    )
    private void onWorldChange(ClientWorld world, CallbackInfo ci) {
        getClientData$reden().getRvcStructures().values().forEach(RvcRepository::clearCache);
        TaskKt.getTaskStack().forEach(it -> it.onClientSideWorldChanged(world));
    }
}
