package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.coroutines.CoroutineDispatcher
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class LocalNetworkWorker(
    override val structure: TrackedStructure,
    override val world: ServerWorld,
    clientWorld: World
) : NetworkWorker {
    private val clientWorker = ClientNetworkWorker(structure, clientWorld)
    private val player = world.server.playerManager.getPlayer(world.server.hostProfile!!.id)!!
    private val serverWorker = ServerNetworkWorker(structure, world, player)

    override fun trackpointUpdated(part: TrackedStructurePart) = clientWorker.trackpointUpdated(part)

    override suspend fun debugRender(part: TrackedStructurePart) = execute {
        clientWorker.debugRender(part)
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) = execute {
        serverWorker.startUndoRecord(cause)
    }

    override suspend fun stopUndoRecord() = execute {
        serverWorker.stopUndoRecord()
    }

    override suspend fun paste(part: TrackedStructurePart) = execute {
        serverWorker.paste(part)
    }

    override val coroutineDispatcher: CoroutineDispatcher get() = serverWorker.coroutineDispatcher
}
