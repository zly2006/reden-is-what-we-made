package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage

class GlobalNetworkStage(
    private val _parent: ServerRootStage
) : TickStage("global_network", _parent) {
    override fun toString() = "GlobalNetworkTick"
}