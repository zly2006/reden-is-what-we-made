package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

private val id = Reden.identifier("breakpoint_interrupt")
private val pType = PacketType.create(id) {
    val id = it.readVarInt()
    BreakPointInterrupt(id)
}

data class BreakPointInterrupt(
    val bpId: Int,
    val interrupted: Boolean = true
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(bpId)
    }

    override fun getType(): PacketType<BreakPointInterrupt> = pType

    companion object {
        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                }
            }
        }
    }
}