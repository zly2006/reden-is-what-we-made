package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.utils.server

object EndStage: TickStage("end") {
    override fun tick() {
        server.runTasksTillTickEnd()
    }

    fun waitAll() {
        server.runTasks { false }
    }
}
