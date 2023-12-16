package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.world.*
import net.minecraft.server.world.ServerWorld
import java.util.function.BooleanSupplier

class WorldRootStage(
    override val world: ServerWorld,
    parent: ServerRootStage,
    val shouldKeepTicking: BooleanSupplier
) : TickStage("world_root", parent = parent), TickStageWithWorld {
    @Deprecated("TickStage is going not to be tickable.")
    fun tick() {
        children.add(WorldBorderStage(this))
        children.add(WeatherStage(this))
        children.add(TimeStage(this))
        children.add(BlockScheduledTicksRootStage(this))
        children.add(FluidScheduledTicksRootStage(this))
        children.add(RaidStage(this))
        //todo: spawn stage and random tick stage
        // profiler.swap("chunkSource");
        postTick()
        children.add(RandomTickStage(this))
        children.add(BlockEventsRootStage(this))
        children.add(EntitiesRootStage(this))
        children.add(BlockEntitiesRootStage(this))
    }

    override fun toString() = "World RootStage/${world.registryKey.value}"
}
