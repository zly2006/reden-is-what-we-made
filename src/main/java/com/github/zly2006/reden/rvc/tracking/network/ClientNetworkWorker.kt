package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.render.BlockOutline
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.coroutines.*
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class ClientNetworkWorker(
    override val structure: TrackedStructure,
    override val world: World
) : NetworkWorker {
    var renderPositions = listOf<BlockPos>()
    override suspend fun debugRender(part: TrackedStructurePart) = execute {
        BlockOutline.blocks = mapOf()
        BlockBorder.tags = mapOf()
        BlockOutline.blocks = renderPositions.mapNotNull {
            if (!world.isAir(it))
                it to world.getBlockState(it)
            else null
        }.toMap()
        part.trackPoints.forEach {
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

    override suspend fun paste(part: TrackedStructurePart) {
        TODO("Not yet implemented")
    }

    override suspend fun <T> execute(function: suspend () -> T): T =
        withContext(MinecraftClient.getInstance().asCoroutineDispatcher()) { function() }

    @OptIn(DelicateCoroutinesApi::class)
    override fun <T> async(function: suspend () -> T) =
        GlobalScope.async(MinecraftClient.getInstance().asCoroutineDispatcher()) { function() }
}
