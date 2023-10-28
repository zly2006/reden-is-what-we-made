package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.ClientData;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(MinecraftClient.class)
public abstract class MixinClient implements ClientData.ClientDataAccess {
    @Unique ClientData clientData = new ClientData((MinecraftClient) (Object) this);

    @NotNull
    @Override
    public ClientData getRedenClientData() {
        return clientData;
    }
}
