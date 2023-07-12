package com.github.zly2006.reden.network

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

abstract class StatusSyncPacket(
    val status: Long,
    val data: NbtCompound?
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarLong(status)
        buf.writeNbt(data)
    }
}
class WorldStatus(status: Long, data: NbtCompound?)
    : StatusSyncPacket(status, data) {
        companion object {
            const val STARTED = 1L
            const val FROZEN = 2L
            val id = Identifier("reden", "world_status")
        }

    override fun getType(): PacketType<*> = PacketType.create(id) {
        val status = it.readVarLong()
        val nbt = it.readNbt()
        WorldStatus(status, data)
    }
}
class GlobalStatus(status: Long, data: NbtCompound?)
    : StatusSyncPacket(status, data) {
    companion object {
        val id = Identifier("reden", "global_status")
    }

    override fun getType(): PacketType<*> = PacketType.create(id) {
        val status = it.readVarLong()
        val nbt = it.readNbt()
        WorldStatus(status, data)
    }
}