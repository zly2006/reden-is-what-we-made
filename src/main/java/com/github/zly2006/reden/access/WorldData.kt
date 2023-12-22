package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.WorldRootStage
import com.github.zly2006.reden.mixinhelper.RedenNeighborUpdater
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class WorldData(
    val serverWorld: ServerWorld
): StatusAccess {
    override var status: Long = 0
    lateinit var tickStage: WorldRootStage
    val redenNeighborUpdater by lazy {
        RedenNeighborUpdater(serverWorld, serverWorld.server.data)
    }

    interface WorldDataAccess {
        fun getRedenWorldData(): WorldData
    }

    companion object {
        @JvmStatic
        val ServerWorld.data: WorldData get() {
            return (this as WorldDataAccess).getRedenWorldData()
        }

        @JvmStatic
        val World.data: WorldData? get() {
            return (this as? ServerWorld)?.data
        }
    }
}
