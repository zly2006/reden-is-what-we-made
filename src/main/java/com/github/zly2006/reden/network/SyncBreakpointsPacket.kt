package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload

class SyncBreakpointsPacket(
    val data: List<BreakPoint>
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<SyncBreakpointsPacket> by PacketCodec(Reden.identifier("sync_breakpoints")) {
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
