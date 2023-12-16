package com.github.zly2006.reden.mixin.debugger.network;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.TickStage;
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

import static com.github.zly2006.reden.access.ServerData.getData;

@Mixin(NetworkThreadUtils.class)
public class MixinNetworkThreadUtils {
    @Inject(
            method = "method_11072",
            at = @At("HEAD")
    )
    private static void begin(PacketListener packetListener, Packet<?> packet, CallbackInfo ci) {
        if (UtilsKt.server != null) {
            ServerData data = getData(UtilsKt.server);
            if (packetListener instanceof ServerPlayNetworkHandler spnh) {
                TickStage stage = data.getTickStageTree().getActiveStage();
                data.getTickStageTree().push$reden_is_what_we_made(
                        new TickStageWorldProvider("network",stage, spnh.getPlayer().getServerWorld())
                );
            }
        }
    }

    @Inject(
            method = "method_11072",
            at = @At("RETURN")
    )
    private static void end(PacketListener packetListener, Packet<?> packet, CallbackInfo ci) {
        if (UtilsKt.server != null) {
            ServerData data = getData(UtilsKt.server);
            if (packetListener instanceof ServerPlayNetworkHandler) {
                data.getTickStageTree().pop$reden_is_what_we_made();
            }
        }
    }
}
