package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.writeBlock
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.BlockEvent

class BlockEventStage(
    val _parent: BlockEventsRootStage,
    var blockEvent: BlockEvent? = null
): TickStage("block_event", _parent) {
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

    override fun tick() {
        assert(children.isEmpty())

        _parent.world.data().tickingBlockEvent = blockEvent!!
        _parent.world.processSyncedBlockEvents()
    }
}
