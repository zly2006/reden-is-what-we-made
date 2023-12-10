package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.network.GlobalStatus;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(value = ServerPlayNetworkHandler.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinServerPlayNetwork {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;syncWithPlayerPosition()V"
            ),
            cancellable = true
    )
    private void tick(CallbackInfo ci) {
        if (data(player.server).hasStatus(GlobalStatus.FROZEN)) {
            ci.cancel();
        }
    }
}
