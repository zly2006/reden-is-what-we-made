package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.WorldRootStage

abstract class AbstractWorldChildStage(
    name: String,
    val _parent: WorldRootStage
): TickStage(name, _parent), TickStageWithWorld {
    override val world get() = _parent.world

    override fun tick() {
        super.tick()
        world.tick(_parent.shouldKeepTicking)
        yield()
    }
}
