package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.utils.isClient
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload

@Serializable
class SyncBreakpointsPacketS2CPacket(
    val data: List<BreakPoint>
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<SyncBreakpointsPacketS2CPacket>
                       by PacketCodec(Reden.identifier("sync_breakpoints_s2c")) {
        fun register() {
            if (isClient) {
                PayloadTypeRegistry.playS2C().register(ID, CODEC)
                ClientPlayNetworking.registerGlobalReceiver(ID) { packet, _ ->
                    val manager = MinecraftClient.getInstance().data.breakpoints
                    manager.clear()
                    packet.data.forEach {
                        manager.breakpointMap[it.id] = it
                    }
                }
            }
        }
    }
}
