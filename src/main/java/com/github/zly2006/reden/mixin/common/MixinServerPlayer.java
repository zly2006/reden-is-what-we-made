package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import com.github.zly2006.reden.render.SolidFaceRenderer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayer implements PlayerData.PlayerDataAccess {
    @Unique PlayerData data = new PlayerData((ServerPlayerEntity)(Object)this);
    @Unique SolidFaceRenderer solidFaceRenderer = new SolidFaceRenderer((ServerPlayerEntity)(Object)this);

    @NotNull
    @Override
    public PlayerData getRedenPlayerData() {
        return data;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onPlayerTicked(CallbackInfo ci) {
        if(RedenCarpetSettings.solidFaceRenderer) {
            this.solidFaceRenderer.tick();
        }
    }
}
