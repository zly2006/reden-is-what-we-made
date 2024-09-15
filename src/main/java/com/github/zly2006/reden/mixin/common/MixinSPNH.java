package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.SPNHAccess;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinSPNH implements SPNHAccess {
    @Shadow
    public ServerPlayerEntity player;
    ServerPlayerEntity behalf;

    public void setBehalfPlayer$reden(ServerPlayerEntity serverPlayer) {
        this.behalf = serverPlayer;
    }

    @Nullable
    public ServerPlayerEntity getBehalfPlayer$reden() {
        return behalf;
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;player:Lnet/minecraft/server/network/ServerPlayerEntity;"
            )
    )
    private ServerPlayerEntity redirectPlayerInstance(ServerPlayNetworkHandler instance) {
        if (this.behalf != null) {
            return this.behalf;
        } else {
            return instance.player;
        }
    }
}
