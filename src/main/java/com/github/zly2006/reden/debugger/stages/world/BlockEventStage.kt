package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.writeBlock
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.BlockEvent
import net.minecraft.text.Text

class BlockEventStage(
    val _parent: BlockEventsRootStage,
    var blockEvent: BlockEvent? = null
): TickStage("block_event", _parent), TickStageWithWorld {
    override val world get() = _parent.world

    override val displayName
        get() = Text.translatable("reden.debugger.tick_stage.$name", blockEvent!!.pos.toShortString(), blockEvent!!.block.name, blockEvent!!.type, blockEvent!!.data)

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        blockEvent = BlockEvent(
            buf.readBlockPos(),
            buf.readBlock(),
            buf.readInt(),
            buf.readInt()
        )
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(blockEvent!!.pos)
        buf.writeBlock(blockEvent!!.block)
        buf.writeInt(blockEvent!!.type)
        buf.writeInt(blockEvent!!.data)
    }

    // Note: tick() method that does not call TickStage#tick
    override fun tick() {
        assert(children.isEmpty())

        _parent.world!!.data().tickingBlockEvent = blockEvent!!
        _parent.world!!.processSyncedBlockEvents()
    }
}
