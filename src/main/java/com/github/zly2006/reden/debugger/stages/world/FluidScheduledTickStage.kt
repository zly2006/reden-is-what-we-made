package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage

class FluidScheduledTickStage(
    val _parent: WorldRootStage
): TickStage("fluid_scheduled_tick", _parent) {
}
