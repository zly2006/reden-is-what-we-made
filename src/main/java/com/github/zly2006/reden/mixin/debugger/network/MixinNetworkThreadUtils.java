package com.github.zly2006.reden.mixin.debugger.network;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.stages.TickStageWorldProvider;
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
            ServerData data = data(UtilsKt.server);
            if (packetListener instanceof ServerPlayNetworkHandler spnh) {
                data.getTickStageTree().insert2child(new TickStageWorldProvider(
                        "network",
                        data.getTickStageTree().peekLeaf(),
                        spnh.getPlayer().getServerWorld()
                ));
                data.getTickStageTree().next().tick();
            }
        }
    }
}
