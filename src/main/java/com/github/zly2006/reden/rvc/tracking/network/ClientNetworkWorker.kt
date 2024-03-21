package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.render.BlockOutline
import com.github.zly2006.reden.rvc.tracking.StructureTracker
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class ClientNetworkWorker(
    override val structure: TrackedStructure,
    override val world: World
) : NetworkWorker {
    private var renderPositions = listOf<BlockPos>()
    override fun trackpointUpdated(part: TrackedStructurePart) {
        if (part.tracker is StructureTracker.Trackpoint) {
            renderPositions = part.tracker.cachedPositions.keys.toList()
        }
    }
    override suspend fun debugRender(part: TrackedStructurePart) = execute {
        BlockOutline.blocks = mapOf()
        BlockBorder.tags = mapOf()
        BlockOutline.blocks = renderPositions.mapNotNull {
            if (!world.isAir(it))
                it to world.getBlockState(it)
            else null
        }.toMap()
        if (part.tracker is StructureTracker.Trackpoint) {
            part.tracker.trackpoints.forEach {
                if (!world.isAir(it.pos))
                    BlockBorder[it.pos] = if (it.mode.isTrack()) 1 else 2
            }
        }
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) {}
    override suspend fun stopUndoRecord() {}
    override suspend fun paste(part: TrackedStructurePart) {
        TODO("Not yet implemented")
    }

    override val coroutineDispatcher = MinecraftClient.getInstance().asCoroutineDispatcher()
}
