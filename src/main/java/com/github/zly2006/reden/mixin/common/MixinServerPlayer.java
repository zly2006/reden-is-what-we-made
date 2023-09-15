package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.PlayerData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayer implements PlayerData.PlayerDataAccess {
    @Unique PlayerData data = new PlayerData((ServerPlayerEntity)(Object)this);

    @NotNull
    @Override
    public PlayerData getRedenPlayerData() {
        return data;
    }
}
