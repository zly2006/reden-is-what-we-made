package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.world.block.NeighborUpdater

class UpdaterData {
    var currentParentTickStage: TickStage? = null

    interface UpdaterDataAccess {
        fun getRedenUpdaterData(): UpdaterData
    }

    companion object {
        @JvmStatic
        fun NeighborUpdater.updaterData(): UpdaterData {
            return (this as UpdaterDataAccess).getRedenUpdaterData()
        }
    }
}