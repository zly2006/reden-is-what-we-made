package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

class SyncBreakpointsPacket(
    val data: Collection<BreakPoint>
): FabricPacket {
    companion object {
        val id = Reden.identifier("sync_breakpoints")
        val pType = PacketType.create(id) {
            val size = it.readVarInt()
            val list = ArrayList<BreakPoint>(size)
            val manager = BreakpointsManager.getBreakpointManager()
            for (i in (0 until size)) {
                list.add(manager.read(it))
            }
            SyncBreakpointsPacket(list)
        }!!

        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                    val manager = BreakpointsManager.getBreakpointManager()
                    manager.clear()
                    packet.data.forEach {
                        manager.breakpointMap[it.id] = it
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(data.size)
        val manager = BreakpointsManager.getBreakpointManager()
        data.forEach { manager.write(buf, it) }
    }

    override fun getType() = pType
}
