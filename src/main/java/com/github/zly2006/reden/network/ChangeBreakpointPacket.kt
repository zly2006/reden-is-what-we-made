package com.github.zly2006.reden.network

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.breakpoints
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import java.util.*
import kotlin.jvm.optionals.getOrNull

private val id = Identifier("reden", "change_breakpoint")
private val pType = PacketType.create(id) {
    val bp = BreakPoint.read(it)
    val flag = it.readVarInt()
    val bpId = it.readVarInt()
    val uuid = it.readOptional { it.readUuid() }.getOrNull()
    ChangeBreakpointPacket(bp, flag, bpId, uuid)
}

data class ChangeBreakpointPacket(
    val breakPoint: BreakPoint,
    val flag: Int = 0,
    val bpId: Int = 0,
    val sender: UUID? = null
): FabricPacket {
    companion object {
        fun register() {
            val action = { packet: ChangeBreakpointPacket, player: PlayerEntity, sender: PacketSender ->
                val bp = packet.breakPoint
                val flag = packet.flag
                val bpId = packet.bpId
                when (flag) {
                    ADD -> breakpoints[bpId] = bp
                    REMOVE -> breakpoints.remove(bpId)
                }
                bp.flags = flag
            }
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                action(packet, player, sender)
                player.server.playerManager.playerList.forEach {
                    ServerPlayNetworking.send(it, packet.copy(sender = player.uuid)) // sync
                }
            }
            ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
                breakpoints.forEach { (id, bp) -> // sync
                    sender.sendPacket(ChangeBreakpointPacket(
                        bp,
                        bp.flags,
                        id
                    ))
                }
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType, action)
                ClientPlayConnectionEvents.JOIN.register { handler, sender, client ->
                    breakpoints.clear()
                }
            }
        }

        const val ADD = 1
        const val REMOVE = 2

        // properties
        const val ENABLED = 4
    }

    override fun write(buf: PacketByteBuf) {
        breakPoint.write(buf)
        buf.writeVarInt(flag)
        buf.writeVarInt(bpId)
        buf.writeOptional(sender) { buf.writeUuid(it) }
    }

    override fun getType(): PacketType<ChangeBreakpointPacket> = pType
}

private fun <T> PacketByteBuf.writeOptional(o: T?, write: (T) -> Unit) {
    if (o == null) {
        writeBoolean(false)
    } else {
        writeBoolean(true)
        write(o)
    }
}
