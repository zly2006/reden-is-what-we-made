package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import net.minecraft.world.tick.OrderedTick

abstract class ScheduledTicksRootStage(
    name: String,
    parent: WorldRootStage
): AbstractWorldChildStage(name, parent) {
    abstract fun createChild(orderedTick: OrderedTick<*>): TickStage
}
