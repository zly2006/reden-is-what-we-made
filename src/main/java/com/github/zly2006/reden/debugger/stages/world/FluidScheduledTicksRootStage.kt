package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import net.minecraft.fluid.Fluid
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.tick.OrderedTick

class FluidScheduledTicksRootStage(
    parent: WorldRootStage
): ScheduledTicksRootStage("fluid_scheduled_ticks_root", parent), TickStageWithWorld {
    override val world: ServerWorld get() = (parent as WorldRootStage).world

    @Suppress("UNCHECKED_CAST")
    override fun createChild(orderedTick: OrderedTick<*>): TickStage {
        return FluidScheduledTickStage(this, orderedTick as OrderedTick<Fluid>)
    }
}
