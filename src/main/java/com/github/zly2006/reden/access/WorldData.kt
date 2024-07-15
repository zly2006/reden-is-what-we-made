package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.mixinhelper.RedenNeighborUpdater
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

@Serializable
class WorldData : StatusAccess {
    @Transient
    var redenNeighborUpdater: RedenNeighborUpdater? = null
    val worldId: String

    constructor(serverWorld: ServerWorld) {
        worldId = buildString {
            append(serverWorld.server.data.serverId)
            append('-')
            append(serverWorld.registryKey.value.toString())
        }
        redenNeighborUpdater = RedenNeighborUpdater(serverWorld, serverWorld.server.data)
    }

    constructor() {
        worldId = ""
    }

    override var status: Long = 0

    @JvmField
    var updatesDisabled = false

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
