package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
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

    override suspend fun refreshPositions() = execute {
        serverWorker.refreshPositions()
        clientWorker.renderPositions = structure.cachedPositions.keys.toList()
    }

    override suspend fun debugRender() = execute {
        clientWorker.debugRender()
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) = execute {
        serverWorker.startUndoRecord(cause)
    }

    override suspend fun stopUndoRecord() = execute {
        serverWorker.stopUndoRecord()
    }

    override suspend fun paste() = execute {
        serverWorker.paste()
    }

    override suspend fun <T> execute(function: suspend () -> T) = serverWorker.execute(function)

    override fun <T> async(function: suspend () -> T) = serverWorker.async(function)
}
