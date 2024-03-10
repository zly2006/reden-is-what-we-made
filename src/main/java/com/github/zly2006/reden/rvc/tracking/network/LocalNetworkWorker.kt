package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import kotlinx.coroutines.runBlocking
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

    override suspend fun refreshPositions() = executeBlocking {
        serverWorker.refreshPositions()
    }

    override suspend fun debugRender() = executeBlocking {
        clientWorker.debugRender()
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) = executeBlocking {
        serverWorker.startUndoRecord(cause)
    }

    override suspend fun stopUndoRecord() = executeBlocking {
        serverWorker.stopUndoRecord()
    }

    override suspend fun paste() = executeBlocking {
        serverWorker.paste()
    }

    override suspend fun <T> execute(function: () -> T): T {
        return serverWorker.execute(function)
    }

    private suspend fun <T> executeBlocking(function: suspend () -> T): T {
        return serverWorker.execute {
            runBlocking {
                function()
            }
        }
    }
}
