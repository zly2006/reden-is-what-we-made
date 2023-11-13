package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage

class BlockEventsRootStage(
    val _parent: WorldRootStage
): TickStage("block_events_root", _parent) {
}