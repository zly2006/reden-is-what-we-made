package com.github.zly2006.reden.debugger.stages.block

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.access.UpdaterData.Companion.updaterData
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.storage.BlocksResetStorage
import com.github.zly2006.reden.network.StageTreeS2CPacket
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.math.BlockPos
import net.minecraft.world.block.ChainRestrictedNeighborUpdater
import net.minecraft.world.block.NeighborUpdater
import net.minecraft.world.block.ChainRestrictedNeighborUpdater as Updater119

abstract class AbstractBlockUpdateStage<T: Updater119.Entry>(
    name: String,
    parent: TickStage
): TickStage(name, parent), TickStageWithWorld {
    override val world get() = (parent as TickStageWithWorld).world
    abstract val entry: T
    val resetStorage = BlocksResetStorage()

    fun checkBreakpoints() {
        if (sourcePos == BlockPos.ORIGIN) {
            //todo: debug
            server.playerManager.playerList.forEach {
                ServerPlayNetworking.send(it, StageTreeS2CPacket(server.data().tickStageTree))
            }
        }
    }

    override fun tick() {
        checkBreakpoints()
        world.neighborUpdater.updaterData().tickEntry(this)
    }

    override fun reset() {
        resetStorage.apply(world)
    }

    abstract val sourcePos: BlockPos
    abstract val targetPos: BlockPos

    companion object {
        @JvmStatic
        fun <T : ChainRestrictedNeighborUpdater.Entry> createStage(updater: NeighborUpdater, entry: T): AbstractBlockUpdateStage<T> {
            val data = updater.updaterData()
            val parent = data.tickStageTree.peekLeaf()
            @Suppress("UNCHECKED_CAST")
            return when (entry) {
                is Updater119.StateReplacementEntry -> StageBlockPPUpdate(parent, entry)
                is Updater119.SixWayEntry -> StageBlockNCUpdateSixWay(parent, entry)
                is Updater119.StatefulEntry -> StageBlockNCUpdateWithSource(parent, entry)
                is Updater119.SimpleEntry -> StageBlockNCUpdate(parent, entry)
                else -> throw IllegalArgumentException("Unknown updater entry type: ${entry.javaClass}")
            } as AbstractBlockUpdateStage<T> // unchecked, but we know it's right
        }
    }
}
