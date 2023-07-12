package com.github.zly2006.reden.debugger.breakpoint

import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrNull

class BlockUpdateEvent(
    id: Int,
    var options: Int = 0,
    var pos: BlockPos? = null,
): BreakPoint(id, Companion) {
    companion object: BreakPointType {
        override val id: Identifier = Identifier("reden", "update_event")
        override val description: Text = Text.literal("BlockUpdateEvent")
        override fun create(id: Int): BreakPoint = BlockUpdateEvent(id)

        const val PP = 1
        const val NC = 2
        const val CU = 4
    }
    override fun read(buf: PacketByteBuf) {
        options = buf.readVarInt()
        pos = buf.readOptional { it.readBlockPos() }.getOrNull()
    }
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(options)
        if (pos == null) {
            buf.writeBoolean(false)
        } else {
            buf.writeBoolean(true)
            buf.writeBlockPos(pos!!)
        }
    }

    override fun toString() = buildString {
        append("BlockUpdateEvent(")
        if (options and PP > 0) {
            append("PP")
        }
        if (options and NC > 0) {
            append("NC")
        }
        if (options and CU > 0) {
            append("CU")
        }
        append(')')
        append(pos)
    }
}
