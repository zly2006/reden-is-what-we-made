package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

abstract class AbstractBlockUpdateStage<T: ChainRestrictedNeighborUpdater.Entry>(
    name: String,
    parent: TickStage
) : TickStage(name, parent) {
    abstract val entry: T

    fun checkBreakpoints() {
    }
}
