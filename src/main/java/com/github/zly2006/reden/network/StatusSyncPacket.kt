package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

abstract class StatusSyncPacket(
    val status: Long,
    val data: NbtCompound?
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarLong(status)
        buf.writeNullable(data, PacketByteBuf::writeNbt)
    }
}
class WorldStatus(status: Long, data: NbtCompound?)
    : StatusSyncPacket(status, data) {
        companion object {
            val id = Reden.identifier("world_status")
            val pType = PacketType.create(id) {
                val status = it.readVarLong()
                val nbt = it.readNullable(PacketByteBuf::readNbt)
                WorldStatus(status, nbt)
            }!!

            fun register() {
                if (isClient) {
                    ClientPlayNetworking.registerGlobalReceiver(pType) { packet, cp, _ ->
                        // todo: client side world data
                        cp.world.data()?.status = packet.status
                    }
                }
            }
        }

    override fun getType() = pType
}
class GlobalStatus(status: Long, data: NbtCompound?)
    : StatusSyncPacket(status, data) {
    companion object {
        const val STARTED = 1L
        const val FROZEN = 2L
        val id = Reden.identifier("global_status")
        val pType = PacketType.create(id) {
            val status = it.readVarLong()
            val nbt = it.readNullable(PacketByteBuf::readNbt)
            GlobalStatus(status, nbt)
        }!!

        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                    MinecraftClient.getInstance().serverData?.status = packet.status
                }
            }
        }
    }

    override fun getType() = pType
}
