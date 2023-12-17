package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import com.github.zly2006.reden.debugger.stages.world.BlockEventsRootStage
import com.github.zly2006.reden.mixinhelper.RedenNeighborUpdater
import net.minecraft.server.world.BlockEvent
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import net.minecraft.world.chunk.BlockEntityTickInvoker

class WorldData(
    val serverWorld: ServerWorld
): StatusAccess {
    override var status: Long = 0
    lateinit var tickStage: WorldRootStage
    @JvmField var tickingBlockEvent: BlockEvent? = null
    @JvmField var blockEventsRootStage: BlockEventsRootStage? = null
    @JvmField var blockEntityTickInvoker: BlockEntityTickInvoker? = null
    val redenNeighborUpdater by lazy {
        RedenNeighborUpdater(serverWorld, serverWorld.server.data)
    }

    interface WorldDataAccess {
        fun getRedenWorldData(): WorldData
    }

    companion object {
        @JvmStatic
        fun ServerWorld.data(): WorldData {
            return (this as WorldDataAccess).getRedenWorldData()
        }
        @JvmStatic
        fun World.data(): WorldData? {
            return (this as? ServerWorld)?.data()
        }
    }
}
