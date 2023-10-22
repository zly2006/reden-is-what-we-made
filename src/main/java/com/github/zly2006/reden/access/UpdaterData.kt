package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.UpdateBlockStage
import com.github.zly2006.reden.utils.server
import net.minecraft.world.block.ChainRestrictedNeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry
import net.minecraft.world.block.NeighborUpdater

class UpdaterData(
    val updater: NeighborUpdater,
) {
    fun yieldUpdater() {

    }

    fun tickNextStage() {
        tickStageTree.next().tick()
    }

    fun tickEntry(entry: Entry) {
        tickingEntry = entry
        thenTickUpdate = true
        when (updater) {
            is ChainRestrictedNeighborUpdater -> {
                updater.runQueuedUpdates()
            }
            else -> {}
        }
    }

    val tickStageTree get() = server.data().tickStageTree

    @JvmField var tickingEntry: Entry? = null
    @JvmField var thenTickUpdate = false
    @JvmField var currentParentTickStage: UpdateBlockStage? = null

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