package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.ServerData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public class MixinServer implements ServerData.ServerDataAccess {
    @Unique ServerData serverData = new ServerData();
    @NotNull
    @Override
    public ServerData getRedenServerData() {
        return serverData;
    }
}
