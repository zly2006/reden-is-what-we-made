package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class ClientNetworkWorker(
    override val structure: TrackedStructure,
    override val world: World
) : NetworkWorker {
    private var renderPositions = listOf<BlockPos>()
    override suspend fun trackpointUpdated(part: TrackedStructurePart) {
        if (part.tracker is StructureTracker.Trackpoint) {
            renderPositions = part.tracker.cachedPositions.keys.toList()
        }
    }
    override suspend fun debugRender(part: TrackedStructurePart) = execute {
        part.tracker.render(part)
    }

    override suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause) {}
    override suspend fun stopUndoRecord() {}
    override suspend fun paste(part: TrackedStructurePart) {
        TODO("Not yet implemented")
    }

    override val coroutineDispatcher = MinecraftClient.getInstance().asCoroutineDispatcher()
}
