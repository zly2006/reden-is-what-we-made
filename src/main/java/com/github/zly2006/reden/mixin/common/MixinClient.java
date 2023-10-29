package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.ClientData;
import com.github.zly2006.reden.access.ServerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(MinecraftClient.class)
public abstract class MixinClient implements ClientData.ClientDataAccess, ServerData.ClientSideServerDataAccess {
    @Shadow @Nullable public ClientPlayerEntity player;
    @Unique ClientData clientData = new ClientData((MinecraftClient) (Object) this);
    @Unique ServerData serverData = null;

    @NotNull
    @Override
    public ClientData getRedenClientData() {
        return clientData;
    }

    @Override
    public ServerData getRedenServerData() {
        return serverData;
    }

    @Override
    public void setRedenServerData(@Nullable ServerData serverData) {
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
        }
    }
}
