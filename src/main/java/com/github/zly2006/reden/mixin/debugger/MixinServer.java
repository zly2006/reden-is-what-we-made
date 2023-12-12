package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.function.CommandFunctionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = MinecraftServer.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServer implements ServerData.ServerDataAccess {
    @Shadow
    @Final
    private CommandFunctionManager commandFunctionManager;

    @Shadow
    @Final
    private List<Runnable> serverGuiTickables;

    @Shadow
    @Final
    private ServerNetworkIo networkIo;
}
