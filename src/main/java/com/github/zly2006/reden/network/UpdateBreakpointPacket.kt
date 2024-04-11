package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.gui.BreakpointUpdatable
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.codec.UUIDSerializer
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload
import java.util.*

private val id = Reden.identifier("update_breakpoint")

@Serializable
data class UpdateBreakpointPacket(
    val breakPoint: BreakPoint?,
    val flag: Int = 0,
    val bpId: Int = 0,
    @Serializable(with = UUIDSerializer::class)
    val sender: UUID? = null
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<UpdateBreakpointPacket> by PacketCodec(id) {
        private fun updateBreakpoint(packet: UpdateBreakpointPacket, manager: BreakpointsManager) {
            val bp = packet.breakPoint
            val flag = packet.flag
            val bpId = packet.bpId
            when {
                (flag and UPDATE != 0) -> manager.breakpointMap[bpId] = bp!!
                (flag and REMOVE != 0) -> manager.breakpointMap.remove(bpId)
            }
            if (flag and REMOVE == 0) {
                manager.breakpointMap[bpId].flags = flag and UPDATE.inv() and REMOVE.inv()
            }
            if (manager.isClient) {
                val screen = MinecraftClient.getInstance().currentScreen
                if (screen is BreakpointUpdatable) {
                    screen.updateBreakpoint(packet)
                }
            }
        }
        fun register() {
            PayloadTypeRegistry.playC2S().register(ID, CODEC)
            ServerPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                updateBreakpoint(packet, context.player().server.data.breakpoints)
                context.player().sendMessage("Breakpoint updated.")
                // sync
                context.player().server.sendToAll(packet.copy(sender = context.player().uuid))
            }
            ServerPlayConnectionEvents.JOIN.register { _, sender, server ->
                server.data.breakpoints.sendAll(sender)
            }
            if (isClient) {
                PayloadTypeRegistry.playS2C().register(ID, CODEC)
                ClientPlayNetworking.registerGlobalReceiver(ID) { packet, _ ->
                    val data = MinecraftClient.getInstance().data
                    updateBreakpoint(packet, data.breakpoints)
                }
                ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
                    client.data.breakpoints.clear()
                }
            }
        }

        // operations
        const val UPDATE = 1
        const val REMOVE = 2

        // properties
        const val ENABLED = 4
    }
}
