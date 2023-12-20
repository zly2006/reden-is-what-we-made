package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import java.util.*

private val id = Reden.identifier("update_breakpoint")
private val pType = PacketType.create(id) {
    val bp = BreakpointsManager.getBreakpointManager().read(it)
    val flag = it.readVarInt()
    val bpId = it.readVarInt()
    val uuid = it.readNullable(PacketByteBuf::readUuid)
    UpdateBreakpointPacket(bp, flag, bpId, uuid)
}

data class UpdateBreakpointPacket(
    val breakPoint: BreakPoint,
    val flag: Int = 0,
    val bpId: Int = 0,
    val sender: UUID? = null
): FabricPacket {
    companion object {
        private fun updateBreakpoint(packet: UpdateBreakpointPacket, manager: BreakpointsManager) {
            val bp = packet.breakPoint
            val flag = packet.flag
            val bpId = packet.bpId
            when (flag) {
                ADD -> manager.breakpointMap[bpId] = bp
                REMOVE -> manager.breakpointMap.remove(bpId)
            }
            bp.flags = flag
        }
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                updateBreakpoint(packet, player.server.data.breakpoints)
                // sync
                player.server.sendToAll(packet.copy(sender = player.uuid))
            }
            ServerPlayConnectionEvents.JOIN.register { _, sender, server ->
                server.data.breakpoints.sendAll(sender)
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                    val data = net.minecraft.client.MinecraftClient.getInstance().data()
                    updateBreakpoint(packet, data.breakpoints)
                }
                ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
                    client.data().breakpoints.clear()
                }
            }
        }

        // operations
        const val ADD = 1
        const val REMOVE = 2

        // properties
        const val ENABLED = 4
    }

    override fun write(buf: PacketByteBuf) {
        BreakpointsManager.getBreakpointManager().write(breakPoint, buf)
        buf.writeVarInt(flag)
        buf.writeVarInt(bpId)
        buf.writeNullable(sender, PacketByteBuf::writeUuid)
    }

    override fun getType(): PacketType<UpdateBreakpointPacket> = pType
}
