package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.breakpointSerializer
import com.github.zly2006.reden.utils.isClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.cbor.Cbor
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf

class SyncBreakpointsPacket(
    val data: List<BreakPoint>
): FabricPacket {
    companion object {
        val id = Reden.identifier("sync_breakpoints")
        val pType = PacketType.create(id) {
            @OptIn(ExperimentalSerializationApi::class)
            val list = Cbor.decodeFromByteArray(ListSerializer(breakpointSerializer()), it.readByteArray())
            SyncBreakpointsPacket(list)
        }!!

        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                    val manager = MinecraftClient.getInstance().data.breakpoints
                    manager.clear()
                    packet.data.forEach {
                        manager.breakpointMap[it.id] = it
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun write(buf: PacketByteBuf) {
        buf.writeByteArray(Cbor.encodeToByteArray(ListSerializer(breakpointSerializer()), data))
    }

    override fun getType() = pType
}
