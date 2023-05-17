package com.github.zly2006.reden.network

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

private val id = Identifier("reden", "status")
private val pType = PacketType.create(id) {
    Status().apply {
        isFreeze = it.readBoolean()
    }
}

class Status: FabricPacket {
    var isFreeze = false
    override fun write(buf: PacketByteBuf) {
        buf.writeBoolean(isFreeze)
    }
    override fun getType(): PacketType<*> = pType
}
