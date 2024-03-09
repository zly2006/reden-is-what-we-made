package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.send
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class LocalNetworkWorker(
    structure: TrackedStructure,
    override val world: ServerWorld,
    val cleintWorld: World
) : ClientNetworkWorker(structure, world) {

    override fun refreshPositions() {
        world.server.send {
            super.refreshPositions()
        }
    }

    override fun debugRender() {
        world.server.send {
            super.debugRender()
        }
    }
}
