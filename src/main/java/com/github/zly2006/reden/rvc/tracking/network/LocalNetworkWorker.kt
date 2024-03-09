package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class LocalNetworkWorker(
    structure: TrackedStructure,
    override val world: ServerWorld,
    val cleintWorld: World
) : ClientNetworkWorker(structure, world) {
    override fun refreshPositions() {
        world.server.execute {
            super.refreshPositions()
        }
    }

    override fun debugRender() {
        world.server.execute {
            super.debugRender()
        }
    }
}
