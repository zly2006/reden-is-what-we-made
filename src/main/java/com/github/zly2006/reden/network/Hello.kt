package com.github.zly2006.reden.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier

private val id = Identifier("reden", "hello")
fun register() {
    ServerPlayNetworking.registerGlobalReceiver(id) { server, player, l, byteBuf, packetSender ->
        packetSender.sendPacket(id, byteBuf)
    }
}

fun send() {
    ClientPlayNetworking.send(id, PacketByteBufs.empty())
}
