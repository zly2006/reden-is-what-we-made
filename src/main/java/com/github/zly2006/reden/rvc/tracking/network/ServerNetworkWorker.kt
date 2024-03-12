package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import kotlinx.coroutines.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld

private const val OFF_THREAD_MESSAGE = "ServerNetworkWorker must be called on the server thread"

class ServerNetworkWorker(
    override val structure: TrackedStructure,
    override val world: ServerWorld,
    val owner: ServerPlayerEntity
) : NetworkWorker {
    override suspend fun debugRender() { /* NOOP */
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) {
        require(world.server.isOnThread) { OFF_THREAD_MESSAGE }
        UpdateMonitorHelper.playerStartRecording(owner, cause)
    }

    override suspend fun stopUndoRecord() {
        require(world.server.isOnThread) { OFF_THREAD_MESSAGE }
        UpdateMonitorHelper.playerStopRecording(owner)
    }

    override suspend fun paste() {
        require(world.server.isOnThread) { OFF_THREAD_MESSAGE }
        structure.world.data!!.updatesDisabled = true
        structure.paste()
        structure.world.data!!.updatesDisabled = false
    }

    override suspend fun <T> execute(function: suspend () -> T): T =
        withContext(world.server.asCoroutineDispatcher()) { function() }

    @OptIn(DelicateCoroutinesApi::class)
    override fun <T> async(function: suspend () -> T) =
        GlobalScope.async(world.server.asCoroutineDispatcher()) { function() }
}
