package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ADD
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier

val registry = mutableMapOf<Identifier, BreakPointType>(

)

interface BreakPointType {
    val id: Identifier
    val description: Text
    fun create(id: Int): BreakPoint
}

abstract class BreakPoint(
    val id: Int,
    open val type: BreakPointType
) {
    /**
     * @see com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion
     */
    var flags = ADD or ENABLED
    open val handler: Collection<BreakPointBehavior> = mutableListOf()
    open fun call() = handler.forEach { it.onBreakPoint(this) }
    abstract fun write(buf: PacketByteBuf)
    abstract fun read(buf: PacketByteBuf)

    companion object {
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
    }
}
