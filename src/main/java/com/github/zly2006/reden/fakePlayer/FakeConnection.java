package com.github.zly2006.reden.fakePlayer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalChannel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.server.ServerNetworkIo;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

public class FakeConnection extends ClientConnection {
    private boolean registered = false;
    public static EventLoop eventLoop = new DefaultEventLoop();
    public FakeConnection() {
        super(NetworkSide.SERVERBOUND);
    }

    public boolean isRegistered() {
        return registered;
    }

    /**
     * {@link ClientConnection#connectLocal(SocketAddress)}
     */
    public void register(@NotNull ServerNetworkIo io) {
        io.getConnections().add(this);
        /*
        LocalChannel localChannel = new LocalChannel();
        eventLoop.register(localChannel);
        this.channel = localChannel;
        this.address = new LocalAddress("reden-" + UUID.randomUUID());
        this.setState(NetworkState.PLAY);
         */

        new Bootstrap().group(LOCAL_CLIENT_IO_GROUP.get()).handler(new ChannelInitializer<>() {
                    protected void initChannel(Channel channel) {
                        ChannelPipeline channelPipeline = channel.pipeline();
                        channelPipeline.addLast("packet_handler", this);
                    }
                })
                .channel(LocalChannel.class)
                .connect(io.bindLocal())
                .syncUninterruptibly();
        this.setState(NetworkState.PLAY);
        this.registered = true;
    }

    @Override
    public void tick() {
        System.out.println("FakeConnection tick");
        super.tick();
    }
}
