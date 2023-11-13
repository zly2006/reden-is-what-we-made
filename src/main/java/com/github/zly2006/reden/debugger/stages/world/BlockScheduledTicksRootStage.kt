package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage

class BlockScheduledTicksRootStage(
    val _parent: WorldRootStage
): TickStage("block_scheduled_ticks_root", _parent) {
}
