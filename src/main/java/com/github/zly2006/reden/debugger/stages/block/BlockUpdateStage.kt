package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.server.world.ServerWorld

class BlockUpdateStage(
    parent: TickStage?
): TickStage("block_update", parent), TickStageWithWorld {
    override val world: ServerWorld?
        get() = (parent as? TickStageWithWorld)?.world

    override fun endTask() {
    }
}
