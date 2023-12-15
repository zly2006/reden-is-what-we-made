package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.server.world.ServerWorld

class TickStageWorldProvider(
    name: String,
    parent: TickStage?,
    override val world: ServerWorld?
): TickStage(name, parent), TickStageWithWorld {
    override fun toString(): String {
        return "general/$name/${world?.registryKey?.value}"
    }
}
