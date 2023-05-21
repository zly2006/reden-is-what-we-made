package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.debugger.breakpoint.behavior.BreakPointBehavior
import com.github.zly2006.reden.network.ChangeBreakpointPacket.Companion.ADD
import com.github.zly2006.reden.network.ChangeBreakpointPacket.Companion.ENABLED
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

val registry = mutableMapOf<Identifier, (Int) -> BreakPoint>(
    BlockUpdateEvent.id to { BlockUpdateEvent(it) }
)

val breakpoints = TreeMap<Int, BreakPoint>()

abstract class BreakPoint(
    val id: Int
) {
    abstract val description: Text
    abstract val identifier: Identifier

    /**
     * @see com.github.zly2006.reden.network.ChangeBreakpointPacket.Companion
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
            return registry[id]?.invoke(bpId)?.apply { read(buf) } ?: throw Exception("Unknown BreakPoint $id")
        }

        fun write(bp: BreakPoint, buf: PacketByteBuf) {
            buf.writeIdentifier(bp.identifier)
            buf.writeVarInt(bp.id)
            bp.write(buf)
        }
    }
}
