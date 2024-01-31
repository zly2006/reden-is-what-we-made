package com.github.zly2006.reden.mixin.realFakePlayer;

import carpet.fakes.ServerPlayerInterface;
import carpet.patches.EntityPlayerMPFake;
import com.github.zly2006.reden.access.IEntityPlayerActionPack;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerNetworkIo.class)
public class MixinServerNetworkIo {
    @Shadow @Final private MinecraftServer server;

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void onTick(CallbackInfo ci) {
        if (RedenCarpetSettings.Options.realFakePlayer) {
            // Player phase
            for (ServerPlayerEntity player : server.getPlayerManager().players) {
                if (player instanceof EntityPlayerMPFake fake) {
                    // do player tick
                    fake.playerTick();
                }
            }
            // Network phase
            for (ServerPlayerEntity player : server.getPlayerManager().players) {
                // tick action pack
                var actionPack = ((ServerPlayerInterface) player).getActionPack();
                ((IEntityPlayerActionPack) actionPack).setNetworkPhase$reden(true);
                actionPack.onUpdate();
                ((IEntityPlayerActionPack) actionPack).setNetworkPhase$reden(false);
            }
        }
    }
}
