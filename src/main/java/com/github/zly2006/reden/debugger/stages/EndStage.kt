package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage

class EndStage(
    private val _parent: ServerRootStage
) : TickStage(name = "end", parent = _parent) {
    override fun toString() = "Server EndStage"
}
