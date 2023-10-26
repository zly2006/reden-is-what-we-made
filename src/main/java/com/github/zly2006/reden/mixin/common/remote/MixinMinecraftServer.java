package com.github.zly2006.reden.mixin.common.remote;

import com.github.zly2006.reden.access.remote.IRemoteServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IRemoteServer {
}
