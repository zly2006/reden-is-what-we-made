package com.github.zly2006.reden.mixin.common.access;

import com.github.zly2006.reden.access.remote.IRemoteWorld;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MixinServerWorld implements IRemoteWorld {
}
