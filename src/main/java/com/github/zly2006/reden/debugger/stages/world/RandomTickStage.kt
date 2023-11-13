package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage

class RandomTickStage(
    val _parent: WorldRootStage
): TickStage("random_tick", _parent) {
}
