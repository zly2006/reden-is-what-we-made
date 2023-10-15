package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.utils.server

class EndStage(
    parent: ServerRootStage
) : TickStage(name = "end", parent = parent) {
    override fun tick() {
        server.runTasksTillTickEnd()
    }

    fun waitAll() {
        server.runTasks { false }
    }
}
