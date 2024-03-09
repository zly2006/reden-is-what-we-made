package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.server.world.ServerWorld

class ServerNetworkWorker(
    override val structure: TrackedStructure,
    override val world: ServerWorld
) : NetworkWorker {
    override fun debugRender() { /* NOOP */
    }
}
