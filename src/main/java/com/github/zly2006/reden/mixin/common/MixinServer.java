package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.network.GlobalStatus;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinServer implements ServerData.ServerDataAccess {
    @Unique ServerData serverData = new ServerData(Reden.MOD_VERSION, (MinecraftServer) (Object) this);

    @Inject(
            method = "stop",
            at = @At("HEAD")
    )
    private void stopping(CallbackInfo ci) {
        serverData.removeStatus(GlobalStatus.FROZEN);
        serverData.removeStatus(GlobalStatus.STARTED);
    }

    @NotNull
    @Override
    public ServerData getServerData$reden() {
        return serverData;
    }
}
