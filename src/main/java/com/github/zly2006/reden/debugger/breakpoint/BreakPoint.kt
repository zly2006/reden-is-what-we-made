package com.github.zly2006.reden.debugger.breakpoint

import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier

val registry = mutableMapOf<Identifier, () -> BreakPoint>(
    BlockUpdateEvent.id to { BlockUpdateEvent() }
)

abstract class BreakPoint {
    abstract val description: Text
    abstract val id: Identifier
    open val handler: Collection<BreakPointBehavior> = mutableListOf()
    open fun call() = handler.forEach { it.onBreakPoint(this) }
    abstract fun write(buf: PacketByteBuf)
    abstract fun read(buf: PacketByteBuf)
    companion object {
        fun read(buf: PacketByteBuf): BreakPoint {
            val id = buf.readIdentifier()
            return registry[id]?.invoke()?.apply { read(buf) } ?: throw Exception("Unknown BreakPoint $id")
        }

        fun write(bp: BreakPoint, buf: PacketByteBuf) {
            buf.writeIdentifier(bp.id)
            bp.write(buf)
        }
    }
}
