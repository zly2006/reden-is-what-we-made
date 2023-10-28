package com.github.zly2006.reden.debugger.breakpoint

import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos

abstract class BlockUpdateEvent(
    id: Int,
    type: BreakPointType,
    var options: Int = 0,
    var pos: BlockPos? = null,
): BreakPoint(id, type, ) {
    companion object {
        const val PP = 1
        const val NC = 2
        const val CU = 4
    }
    override fun read(buf: PacketByteBuf) {
        options = buf.readVarInt()
        pos = buf.readNullable(PacketByteBuf::readBlockPos)
    }
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(options)
        buf.writeNullable(pos, PacketByteBuf::writeBlockPos)
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
        append(pos?.toShortString())
    }
}
