package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.debugger.stages.block.NeighborChanged
import com.github.zly2006.reden.debugger.stages.block.StageBlockPPUpdate
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos

abstract class BlockUpdateEvent(
    id: Int,
    type: BreakPointType,
    var options: Int = 0,
    override var pos: BlockPos? = null,
): BreakPoint(id, type) {
    companion object {
        const val PP = 1
        const val NC = 2
        // todo
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

    override fun call(event: Any) {
        if (event !is AbstractBlockUpdateStage<*>) {
            throw RuntimeException("BlockUpdateEvent can only be called by AbstractBlockUpdateStage")
        }
        if (options and PP > 0 && event is StageBlockPPUpdate) {
            super.call(event)
        }
        if (options and NC > 0 && event is NeighborChanged) {
            super.call(event)
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
        append(pos?.toShortString())
    }
}
