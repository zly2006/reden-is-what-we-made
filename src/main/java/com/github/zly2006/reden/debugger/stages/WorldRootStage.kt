package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.world.*
import com.github.zly2006.reden.utils.server
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.spawner.SpecialSpawner
import java.util.function.BooleanSupplier

class WorldRootStage(
    override val world: ServerWorld,
    parent: ServerRootStage,
    val shouldKeepTicking: BooleanSupplier
) : TickStage("world_root", parent = parent), TickStageWithWorld {
    var tickLabel = -1
    @JvmField var tickingSpawner: SpecialSpawner? = null
    companion object {
        const val TICK_TIME = 0
    }
    override fun tick() {
        super.tick()
        tickLabel = 0
        // tick the world
        server.tickWorlds(shouldKeepTicking)
        children.add(WorldBorderStage(this))
        children.add(WeatherStage(this))
        children.add(TimeStage(this))
        children.add(BlockScheduledTicksRootStage(this))
        children.add(FluidScheduledTicksRootStage(this))
        children.add(RaidStage(this))

        //todo: spawn stage and random tick stage
        // profiler.swap("chunkSource");
        children.add(RandomTickStage(this))

        if (world.data().blockEventsRootStage == null) {
            // Note: init only
            world.data().blockEventsRootStage = BlockEventsRootStage(this)
        }
        children.add(world.data().blockEventsRootStage!!)
        children.add(EntitiesRootStage(this))
        children.add(BlockEntitiesRootStage(this))
        yield()
    }

    override fun reset() {
        tickLabel = 0
    }

    override fun toString() = "World RootStage/${world.registryKey.value}"
}
