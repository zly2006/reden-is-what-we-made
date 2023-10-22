package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.utils.server
import net.minecraft.server.world.ServerWorld
import java.util.function.BooleanSupplier

class WorldRootStage(
    override val world: ServerWorld,
    parent: ServerRootStage,
    val shouldKeepTicking: BooleanSupplier
) : TickStage("world_root", parent = parent), TickStageWithWorld {
    var tickLabel = -1
    companion object {
        const val TICK_TIME = 0
    }
    override fun tick() {
        tickLabel = 0
        // tick the world
        server.tickWorlds(shouldKeepTicking)
    }

    fun yieldAndTick() {
        world.tick(shouldKeepTicking)
    }

    override fun reset() {
        tickLabel = 0
    }

    override fun toString() = "World RootStage/${world.registryKey.value}"
}
