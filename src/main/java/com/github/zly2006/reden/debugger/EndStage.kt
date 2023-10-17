package com.github.zly2006.reden.debugger

class EndStage(
    private val _parent: ServerRootStage
) : TickStage(name = "end", parent = _parent) {
    override fun tick() {
        _parent.server.runTasksTillTickEnd()
    }

    fun waitAll() {
        _parent.server.runTasks { false }
    }
}
