package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.WorldTickSchedulerAccess
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.writeBlock
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.world.tick.OrderedTick
import net.minecraft.world.tick.TickPriority
import net.minecraft.world.tick.WorldTickScheduler

class BlockScheduledTickStage(
    val _parent: BlockScheduledTicksRootStage,
    var orderedTick: OrderedTick<Block>?,
): TickStage("block_scheduled_tick", _parent) {
    @Suppress("UNCHECKED_CAST")
    override fun tick() {
        val scheduler = _parent.world.blockTickScheduler
        scheduler as WorldTickSchedulerAccess<Block>
        scheduler.setTickingTick(orderedTick!!)
        scheduler.tick(_parent.world::tickBlock)
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlock(orderedTick!!.type)
        buf.writeBlockPos(orderedTick!!.pos)
        buf.writeLong(orderedTick!!.triggerTick)
        buf.writeEnumConstant(orderedTick!!.priority)
        buf.writeLong(orderedTick!!.subTickOrder)
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        orderedTick = OrderedTick(
            buf.readBlock(),
            buf.readBlockPos(),
            buf.readLong(),
            buf.readEnumConstant(TickPriority::class.java),
            buf.readLong(),
        )
    }
}
