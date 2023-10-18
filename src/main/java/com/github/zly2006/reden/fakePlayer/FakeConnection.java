package com.github.zly2006.reden.fakePlayer;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.ServerNetworkIo;

public class FakeConnection extends ClientConnection {
    private boolean registered = false;
    public FakeConnection() {
        super(NetworkSide.SERVERBOUND);
    }

    public boolean isRegistered() {
        return registered;
    }

    public void register(ServerNetworkIo io) {
        io.getConnections().add(this);
        registered = true;
    }
}
