package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.access.UpdaterData.Companion.updaterData
import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.world.block.NeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater as Updater119

abstract class AbstractBlockUpdateStage<T: Updater119.Entry>(
    name: String,
    parent: TickStage
) : TickStage(name, parent) {
    abstract val entry: T

    fun checkBreakpoints() {
    }

    companion object {
        @JvmStatic
        fun createStage(updater: NeighborUpdater, entry: Updater119.Entry): TickStage {
            val data = updater.updaterData()
            val parent = data.currentParentTickStage!!
            return when (entry) {
                is Updater119.StateReplacementEntry -> StageBlockPPUpdate(parent, entry)
                is Updater119.SixWayEntry -> StageBlockNCUpdateSixWay(parent, entry)
                is Updater119.StatefulEntry -> StageBlockNCUpdateWithSource(parent, entry)
                is Updater119.SimpleEntry -> StageBlockNCUpdate(parent, entry)
                else -> throw IllegalArgumentException("Unknown updater entry type: ${entry.javaClass}")
            }
        }
    }
}
