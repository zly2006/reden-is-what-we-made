package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.utils.codec.NbtSerializer
import com.github.zly2006.reden.utils.isClient
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.CustomPayload

@Serializable
abstract class StatusSyncPacket(
    val status: Long,
    @Serializable(with = NbtSerializer::class)
    val data: NbtCompound?
) : CustomPayload {
}

object WorldStatus : PacketCodecHelper<StatusSyncPacket> by PacketCodec(Reden.identifier("world_status")) {
    fun register() {
        if (isClient) {
            PayloadTypeRegistry.playS2C().register(ID, CODEC)
            ClientPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                // todo: client side world data
                MinecraftClient.getInstance().world?.data?.status = packet.status
            }
        }
    }

    fun packet(status: Long, data: NbtCompound): StatusSyncPacket {
        return object : StatusSyncPacket(status, data) {
            override fun getId() = ID
        }
    }
}

object GlobalStatus : PacketCodecHelper<StatusSyncPacket> by PacketCodec(Reden.identifier("global_status")) {
    fun register() {
        if (isClient) {
            PayloadTypeRegistry.playS2C().register(ID, CODEC)
            ClientPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                // todo: client side world data
                MinecraftClient.getInstance().serverData?.status = packet.status
            }
        }
    }

    fun packet(status: Long, data: NbtCompound): StatusSyncPacket {
        return object : StatusSyncPacket(status, data) {
            override fun getId() = ID
        }
    }

    const val STARTED = 1L
    const val FROZEN = 2L
}
