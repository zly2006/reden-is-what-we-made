package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ADD
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier

interface BreakPointType {
    val id: Identifier
    val description: Text
    fun create(id: Int): BreakPoint
}

abstract class BreakPoint(
    val id: Int,
    open val type: BreakPointType
) {
    var world: Identifier? = null
    /**
     * @see com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion
     */
    var flags = ADD or ENABLED
    open val handler: Collection<BreakPointBehavior> = mutableListOf()
    open fun call() = handler.forEach { it.onBreakPoint(this) }
    abstract fun write(buf: PacketByteBuf)
    abstract fun read(buf: PacketByteBuf)
}
