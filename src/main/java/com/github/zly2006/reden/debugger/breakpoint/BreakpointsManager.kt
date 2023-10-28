package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.network.UpdateBreakpointPacket
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry as UpdaterEntry

class BreakpointsManager {
    val registry = mutableMapOf<Identifier, BreakPointType>(

    )

    fun read(buf: PacketByteBuf): BreakPoint {
        val id = buf.readIdentifier()
        val bpId = buf.readVarInt()
        val flags = buf.readVarInt()
        return registry[id]?.create(bpId)?.apply {
            this.flags = flags
            read(buf)
        } ?: throw Exception("Unknown BreakPoint $id")
    }

    fun write(bp: BreakPoint, buf: PacketByteBuf) {
        buf.writeIdentifier(bp.type.id)
        buf.writeVarInt(bp.id)
        buf.writeVarInt(bp.flags)
        bp.write(buf)
    }

    fun sendAll(sender: PacketSender) {
        // todo: send them in 1 packet to reduce network traffic
        breakpointMap.forEach { (id, bp) ->
            sender.sendPacket(UpdateBreakpointPacket(bp, UpdateBreakpointPacket.ADD, id))
        }
    }

    fun clear() {
        breakpointMap.clear()
    }

    fun <T : UpdaterEntry> checkBreakpointsForUpdating(stage: AbstractBlockUpdateStage<T>) {
        val worldId = stage.world?.registryKey?.value
        breakpointMap.values.asSequence().filter { worldId == it.world }.forEach {
            it.call()
        }
    }

    val breakpointMap = Int2ObjectOpenHashMap<BreakPoint>()

    companion object {
        fun getBreakpointManager() = if (isClient) {
            MinecraftClient.getInstance().data().breakpoints
        } else {
            server.data().breakpoints
        }
    }
}
