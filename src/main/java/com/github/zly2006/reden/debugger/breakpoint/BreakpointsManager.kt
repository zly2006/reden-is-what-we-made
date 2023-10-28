package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.network.UpdateBreakpointPacket
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.fabricmc.fabric.api.networking.v1.PacketSender

class BreakpointsManager {
    fun sendAll(sender: PacketSender) {
        breakpointMap.forEach { (id, bp) ->
            sender.sendPacket(UpdateBreakpointPacket(bp, UpdateBreakpointPacket.ADD, id))
        }
    }

    fun clear() {
        breakpointMap.clear()
    }

    val breakpointMap = Int2ObjectOpenHashMap<BreakPoint>()
}