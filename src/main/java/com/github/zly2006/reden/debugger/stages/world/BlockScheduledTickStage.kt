package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.writeBlock
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class BlockScheduledTickStage(
    val _parent: BlockScheduledTicksRootStage,
    var pos: BlockPos?,
    var block: Block?
): TickStage("block_scheduled_tick", _parent), TickStageWithWorld {
    override val displayName: MutableText get() =
        Text.translatable("reden.debugger.tick_stage.block_scheduled_tick", pos?.toShortString())
    override val world get() = _parent.world

    override fun preTick() {
        world!!.server.data.breakpoints.checkBreakpointsForScheduledTick()
        super.preTick()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(pos)
        buf.writeBlock(block!!)
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        pos = buf.readBlockPos()
        block = buf.readBlock()
    }
}
