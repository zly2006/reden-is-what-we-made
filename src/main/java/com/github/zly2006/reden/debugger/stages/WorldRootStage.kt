package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.server.world.ServerWorld
import java.util.function.BooleanSupplier

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

    fun yieldAndTick(shouldKeepTicking: BooleanSupplier) {
        world.tick(shouldKeepTicking)
    }

    override fun reset() {
        tickLabel = 0
    }
}
