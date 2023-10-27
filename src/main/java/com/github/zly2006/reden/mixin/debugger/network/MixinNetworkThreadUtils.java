package com.github.zly2006.reden.mixin.debugger.network;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.stages.GlobalNetworkStage;
import com.github.zly2006.reden.debugger.stages.NetworkStage;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(NetworkThreadUtils.class)
public class MixinNetworkThreadUtils {
    @Inject(
            method = "method_11072",
            at = @At("HEAD")
    )
    private static void begin(PacketListener packetListener, Packet<?> packet, CallbackInfo ci) {
        if (UtilsKt.server != null) {
            ServerData serverData = data(UtilsKt.server);
            if (packetListener instanceof ServerPlayNetworkHandler spnh) {
                // todo: this GlobalNetworkStage is not correct, just make the compiler happy
                serverData.getTickStageTree().insert2child(new NetworkStage(new GlobalNetworkStage(serverData.getTickStage()), spnh.connection));

                // Note: don't tick here, network stage has a side effect
                serverData.getTickStageTree().next();
            }
        }
    }
}
