package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.world.block.NeighborUpdater

class UpdaterData {
    val currentParentTickStage: TickStage? = null

    interface UpdaterDataAccess {
        fun getRedenUpdaterData(): UpdaterData
    }

    companion object {
        fun NeighborUpdater.updaterData(): UpdaterData {
            return (this as UpdaterDataAccess).getRedenUpdaterData()
        }
    }
}