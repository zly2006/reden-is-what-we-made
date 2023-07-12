package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.ServerData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MixinServer implements ServerData.ServerDataAccess {
    ServerData serverData = new ServerData();
    @NotNull
    @Override
    public ServerData getRedenServerData() {
        return serverData;
    }
}
