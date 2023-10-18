package com.github.zly2006.reden.mixin.fakePlayer;

import com.github.zly2006.reden.fakePlayer.FakeNetworkHandler;
import com.github.zly2006.reden.fakePlayer.RedenFakePlayer;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {
    @Shadow
    public abstract MinecraftServer getServer();

    private boolean isFakePlayer = false;

    @Inject(
            method = "onPlayerConnect",
            at = @At("HEAD")
    )
    private void detectFakePlayer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        isFakePlayer = player instanceof RedenFakePlayer;
    }

    @ModifyVariable(
            method = "onPlayerConnect",
            at = @At(
                    value = "STORE"
            )
    )
    private UserCache dontChangeUserCache(UserCache cache) {
        if (isFakePlayer)
            return null;
        return cache;
    }

    @ModifyVariable(
            method = "onPlayerConnect",
            at = @At(
                    value = "STORE"
            )
    )
    private ServerPlayNetworkHandler createFakeNetworkHandler(ServerPlayNetworkHandler old) {
        if (isFakePlayer) {
            return new FakeNetworkHandler(getServer(), old.connection, old.player);
        }
        return old;
    }
}
