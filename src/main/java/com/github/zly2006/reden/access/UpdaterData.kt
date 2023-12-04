package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.utils.server
import net.minecraft.world.block.ChainRestrictedNeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry
import net.minecraft.world.block.NeighborUpdater

class UpdaterData(
    val updater: NeighborUpdater,
) {
    @JvmField var tickingStage: AbstractBlockUpdateStage<*>? = null
    @JvmField var notifyMixinsOnly = false

    fun tickEntry(stage: AbstractBlockUpdateStage<*>) {
        tickingStage = stage
        notifyMixinsOnly = true
        when (updater) {
            is ChainRestrictedNeighborUpdater -> {
                updater.runQueuedUpdates()
            }
            else -> {}
        }
        tickingStage = null
        notifyMixinsOnly = false
    }

    val tickStageTree get() = server.data().tickStageTree
    val tickingEntry: Entry? get() = tickingStage?.entry

    interface UpdaterDataAccess {
        fun yieldUpdater()
        fun getRedenUpdaterData(): UpdaterData
    }

    companion object {
        @JvmStatic
        fun NeighborUpdater.updaterData(): UpdaterData {
            return (this as UpdaterDataAccess).getRedenUpdaterData()
        }
    }
}
