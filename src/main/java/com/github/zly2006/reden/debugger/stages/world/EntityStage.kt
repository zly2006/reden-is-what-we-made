package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage

class EntityStage(
    val _parent: WorldRootStage
): TickStage("entity", _parent) {
}
