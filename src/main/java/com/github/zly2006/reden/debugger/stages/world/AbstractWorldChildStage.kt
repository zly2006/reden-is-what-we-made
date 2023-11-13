package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.WorldRootStage

abstract class AbstractWorldChildStage(name: String, val _parent: WorldRootStage) : TickStage(name, _parent) {
    override fun tick() {
        super.tick()
        _parent.world.tick(_parent.shouldKeepTicking)
    }
}
