package com.github.zly2006.reden.fakePlayer;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class FakeNetworkHandler extends ServerPlayNetworkHandler {
    private static ClientConnection checkConnection(ClientConnection connection) {
        if (!(connection instanceof FakeConnection fakeConnection)) {
            throw new RuntimeException("connection is not a FakeConnection");
        }
        if (!fakeConnection.isRegistered()) {
            throw new RuntimeException("connection is not registered");
        }
        return connection;
    }
    public FakeNetworkHandler(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player) {
        super(server, checkConnection(connection), player);
    }

    @Override
    public void sendPacket(Packet<?> packet) {
    }
}
