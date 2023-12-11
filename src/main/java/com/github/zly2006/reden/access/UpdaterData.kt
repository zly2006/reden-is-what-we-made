package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.utils.server
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry
import net.minecraft.world.block.NeighborUpdater

class UpdaterData(
    val updater: NeighborUpdater,
) {
    @JvmField
    var tickingStage: AbstractBlockUpdateStage<*>? = null
    @JvmField @Deprecated("removed")
    var notifyMixinsOnly = false
    @Deprecated("")
    val tickStageTree get() = server.data().stageTree
    @Deprecated("removed", ReplaceWith("tickingStage?.entry"))
    val tickingEntry: Entry? get() = tickingStage?.entry

    @Deprecated("removed")
    interface UpdaterDataAccess_ {
        fun getRedenUpdaterData(): UpdaterData
    }

    companion object {
        @JvmStatic
        fun NeighborUpdater.updaterData(): UpdaterData {
            return (this as UpdaterDataAccess_).getRedenUpdaterData()
        }
    }
}
