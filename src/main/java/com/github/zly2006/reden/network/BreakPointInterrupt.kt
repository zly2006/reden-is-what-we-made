package com.github.zly2006.reden.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

private val id = Identifier("reden", "breakpoint_interrupt")
private val pType = PacketType.create(id) {
    val id = it.readVarInt()
    BreakPointInterrupt(id)
}

class BreakPointInterrupt(
    val bpId: Int
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(bpId)
    }

    override fun getType(): PacketType<BreakPointInterrupt> = pType

    companion object {
        fun register() {
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
            }
        }
    }
}