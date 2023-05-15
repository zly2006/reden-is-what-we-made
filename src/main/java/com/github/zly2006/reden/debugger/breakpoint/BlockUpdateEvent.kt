package com.github.zly2006.reden.debugger.breakpoint

import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlin.jvm.optionals.getOrNull

class BlockUpdateEvent(
    var pos: BlockPos?,
    var type: Int = 0
): BreakPoint() {
    override val id: Identifier = Identifier("reden", "update_event")
    override val description: Text = Text.literal(toString())
    override fun read(buf: PacketByteBuf) {
        type = buf.readVarInt()
        pos = buf.readOptional { it.readBlockPos() }.getOrNull()
    }
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(type)
        if (pos == null) {
            buf.writeBoolean(false)
        } else {
            buf.writeBoolean(true)
            buf.writeBlockPos(pos!!)
        }
    }

    override fun toString() = buildString {
        append("BlockUpdateEvent(")
        if (type and PP > 0) {
            append("PP")
        }
        if (type and NC > 0) {
            append("NC")
        }
        if (type and CU > 0) {
            append("CU")
        }
        append(')')
        append(pos)
    }


    companion object {
        const val PP = 1
        const val NC = 2
        const val CU = 4
    }
}
