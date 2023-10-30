package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.stages.WorldRootStage
import com.github.zly2006.reden.mixinhelper.BreakpointHelper
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class WorldData(
    val serverWorld: ServerWorld
): StatusAccess {
    override var status: Long = 0
    lateinit var tickStage: WorldRootStage
    val breakpointHelper = BreakpointHelper(serverWorld)
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

interface StatusAccess {
    var status: Long
}

fun StatusAccess.addStatus(status: Long): Long {
    this.status = this.status or status
    return this.status
}

fun StatusAccess.removeStatus(status: Long): Long {
    this.status = this.status and status.inv()
    return this.status
}
