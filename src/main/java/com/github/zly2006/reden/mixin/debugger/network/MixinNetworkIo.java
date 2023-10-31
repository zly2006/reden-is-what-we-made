package com.github.zly2006.reden.mixin.debugger.network;


import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.GlobalNetworkStage;
import com.github.zly2006.reden.debugger.stages.NetworkStage;
import com.github.zly2006.reden.mixin.debugger.MixinServer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

import static com.github.zly2006.reden.access.ServerData.data;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(value = ServerNetworkIo.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public class MixinNetworkIo {
    @Shadow @Final MinecraftServer server;

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final private List<ClientConnection> connections;

    /**
     * Called by {@link MixinServer#tickWorlds(BooleanSupplier)} iff {@code stage instanceof GlobalNetworkStage}
     * <br>
     * Called by {@link NetworkStage#tick()}
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public void tick() {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.connections) {
            @SuppressWarnings("unused") // Leave variables for other mods to inject
            Iterator<ClientConnection> iterator;
            ClientConnection clientConnection;

            TickStage tickStage = data(server).getTickStageTree().peekLeaf();
            Reden.LOGGER.trace("[ServerNetworkIo#tick] tickStage = " + tickStage);
            if (tickStage instanceof GlobalNetworkStage) {
                return;
            }
            NetworkStage stage = (NetworkStage) tickStage;
            clientConnection = stage.getConnection();
            //
            if (clientConnection.isOpen()) {
                try {
                    clientConnection.tick();
                } catch (Exception var7) {
                    if (clientConnection.isLocal()) {
                        throw new CrashException(CrashReport.create(var7, "Ticking memory connection"));
                    }

                    LOGGER.warn("Failed to handle packet for {}", clientConnection.getAddress(), var7);
                    Text text = Text.literal("Internal server error");
                    clientConnection.send(new DisconnectS2CPacket(text), PacketCallbacks.always(() ->
                            clientConnection.disconnect(text)));
                    clientConnection.disableAutoRead();
                }
            } else {
                connections.remove(clientConnection);
                clientConnection.handleDisconnection();
            }
        }
    }
}
