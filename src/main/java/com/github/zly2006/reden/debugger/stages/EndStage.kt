package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage

class EndStage(
    private val _parent: ServerRootStage
) : TickStage(name = "end", parent = _parent) {
    override fun tick() {
        _parent.server.tickWorlds(_parent.shouldKeepTicking)
    }

    fun waitAll() {
        _parent.server.runTasks { false }
    }

    override fun toString() = "Server EndStage"
}
