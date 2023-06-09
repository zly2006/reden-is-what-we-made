package com.github.zly2006.reden.network

import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.loader.api.Version
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

private val id = Identifier("reden", "hello")
private val pType = PacketType.create(id) {
    Hello(Version.parse(it.readString()))
}

class Hello(
    val version: Version
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(version.toString())
    }
    override fun getType(): PacketType<*> = pType
}
