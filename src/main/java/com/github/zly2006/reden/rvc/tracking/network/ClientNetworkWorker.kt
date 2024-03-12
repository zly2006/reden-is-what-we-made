package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.render.BlockOutline
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import kotlinx.coroutines.*
import net.minecraft.client.MinecraftClient
import net.minecraft.world.World

open class ClientNetworkWorker(
    override val structure: TrackedStructure,
    override val world: World
) : NetworkWorker {
    override suspend fun debugRender() {
        BlockOutline.blocks = mapOf()
        BlockBorder.tags = mapOf()
        BlockOutline.blocks = structure.cachedPositions.mapNotNull {
            if (!world.isAir(it.key))
                it.key to world.getBlockState(it.key)
            else null
        }.toMap()
        structure.trackPoints.forEach {
            if (!world.isAir(it.pos))
                BlockBorder[it.pos] = if (it.mode.isTrack()) 1 else 2
        }
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) {
        TODO("Not yet implemented")
    }

    override suspend fun stopUndoRecord() {
        TODO("Not yet implemented")
    }

    override suspend fun paste() {
        TODO("Not yet implemented")
    }

    override suspend fun <T> execute(function: suspend () -> T): T =
        withContext(MinecraftClient.getInstance().asCoroutineDispatcher()) { function() }

    @OptIn(DelicateCoroutinesApi::class)
    override fun <T> async(function: suspend () -> T) =
        GlobalScope.async(MinecraftClient.getInstance().asCoroutineDispatcher()) { function() }
}
