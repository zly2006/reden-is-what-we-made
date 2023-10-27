package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage

class GlobalNetworkStage(
    private val _parent: ServerRootStage
) : TickStage("global_network", _parent) {
    val io get() = _parent.server.networkIo!!
    override fun tick() {
        super.tick()
        synchronized(_parent.server.networkIo!!.connections) {
            _parent.server.networkIo!!.connections.filter { !it.isChannelAbsent }.forEach {
                children.add(NetworkStage(this, it))
            }
        }

        _parent.server.profiler.swap("connection")
        // this.getNetworkIo().tick()
    }

    override fun toString() = "GlobalNetworkTick"
}