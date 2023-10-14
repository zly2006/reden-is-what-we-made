package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf

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
            val id = Reden.identifier("world_status")
        }

    override fun getType(): PacketType<*> = PacketType.create(id) {
        val status = it.readVarLong()
        val nbt = it.readNbt()
        WorldStatus(status, nbt)
    }
}
class GlobalStatus(status: Long, data: NbtCompound?)
    : StatusSyncPacket(status, data) {
    companion object {
        val id = Reden.identifier("global_status")
    }

    override fun getType(): PacketType<*> = PacketType.create(id) {
        val status = it.readVarLong()
        val nbt = it.readNbt()
        GlobalStatus(status, nbt)
    }
}