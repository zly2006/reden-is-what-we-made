package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.PlayerData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayer implements PlayerData.PlayerDataAccess {
    @Unique PlayerData data = new PlayerData((ServerPlayerEntity)(Object)this);

    @NotNull
    @Override
    public PlayerData getRedenPlayerData() {
        return data;
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;networkHandler:Lnet/minecraft/server/network/ServerPlayNetworkHandler;"
            )
    )
    private ServerPlayNetworkHandler networkHandler(ServerPlayerEntity player) {
        if (data.getBehalfBy() != null) {
            return data.getBehalfBy().networkHandler;
        }
        return player.networkHandler;
    }
}
