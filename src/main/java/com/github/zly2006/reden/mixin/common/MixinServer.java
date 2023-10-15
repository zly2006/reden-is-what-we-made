package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.ServerData;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinServer implements ServerData.ServerDataAccess {
    @Unique ServerData serverData = new ServerData((MinecraftServer) (Object) this);

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void beforeTick(CallbackInfo ci) {
        serverData.getTickStage().tick();
    }

    @NotNull
    @Override
    public ServerData getRedenServerData() {
        return serverData;
    }
}
