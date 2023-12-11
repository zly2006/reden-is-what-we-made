package com.github.zly2006.reden.mixin.debugger.network;

import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.EndStage;
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
                assert data.getTickStage() != null;
                TickStage stage = data.getTickStageTree().peekLeaf();
                while (!(stage instanceof EndStage)) {
                    stage = stage.getParent();
                    if (stage == null) throw new RuntimeException("TickStageTree is broken! failed to find an end stage.");
                }
                data.getTickStageTree().insert2child(
                        stage,
                        new TickStageWorldProvider("network",stage, spnh.getPlayer().getServerWorld())
                );
                data.getTickStageTree().next().tick();
            }
        }
    }
}
