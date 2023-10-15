package com.github.zly2006.reden.debugger

import net.minecraft.server.world.ServerWorld

class WorldRootStage(
    val world: ServerWorld,
    parent: ServerRootStage,
) : TickStage("world_root", parent = parent) {
    var tickLabel = -1
    companion object {
        const val TICK_TIME = 0
    }
    override fun tick() {
        tickLabel = 0
        // do nothing, let it yield
    }

    override fun hasNext(): Boolean {
        return super.hasNext()
    }

    override fun reset() {
        tickLabel = 0
    }
}
