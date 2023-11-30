package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import net.minecraft.block.Block
import net.minecraft.world.tick.OrderedTick

class BlockScheduledTicksRootStage(
    parent: WorldRootStage?
): ScheduledTicksRootStage("block_scheduled_ticks_root", parent) {
    @Suppress("UNCHECKED_CAST")
    override fun createChild(orderedTick: OrderedTick<*>): TickStage {
        return BlockScheduledTickStage(this, orderedTick as OrderedTick<Block>)
    }
}
