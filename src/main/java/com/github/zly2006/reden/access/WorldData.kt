package com.github.zly2006.reden.access

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.mixinhelper.RedenNeighborUpdater
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import okhttp3.internal.toHexString

class WorldData(
    val serverWorld: ServerWorld
): StatusAccess {
    override var status: Long = 0

    @JvmField
    var updatesDisabled = false
    val worldId = buildString {
        append(serverWorld.server.session.directory.path.hashCode().toHexString())
        append('-')
        append(serverWorld.registryKey.value.toString())
    }
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
