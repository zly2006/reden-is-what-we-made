package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.UpdateBlockStage
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
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

    fun tickEntry(stage: AbstractBlockUpdateStage<*>) {
        tickingStage = stage
        thenTickUpdate = true
        when (updater) {
            is ChainRestrictedNeighborUpdater -> {
                updater.runQueuedUpdates()
            }
            else -> {}
        }
    }

    fun appendStage(stage: TickStage) {
        tickStageTree.insert2child(
            tickingStage ?: currentParentTickStage ?: error("currentParentTickStage of the NeighborUpdater is null, is it initialized?"),
            stage
        )
    }

    val tickStageTree get() = server.data().tickStageTree

    val tickingEntry: Entry? get() = tickingStage?.entry
    @JvmField var tickingStage: AbstractBlockUpdateStage<*>? = null
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