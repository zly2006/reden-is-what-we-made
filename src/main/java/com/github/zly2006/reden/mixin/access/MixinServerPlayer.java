package com.github.zly2006.reden.mixin.access;

import com.github.zly2006.reden.access.PlayerData;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer implements PlayerData.PlayerDataAccess {
    @Unique PlayerData data = new PlayerData((ServerPlayer)(Object)this);

    @NotNull
    @Override
    public PlayerData reden$playerData() {
        return data;
    }
}
