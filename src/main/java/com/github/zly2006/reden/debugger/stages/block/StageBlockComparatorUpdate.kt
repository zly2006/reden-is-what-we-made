package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

class StageBlockComparatorUpdate(
    parent: TickStage,
    entry: ChainRestrictedNeighborUpdater.SimpleEntry?
): StageBlockNCUpdate(parent, entry, "cu_update") {
}
