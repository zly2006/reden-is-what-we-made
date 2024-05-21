package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
import kotlinx.coroutines.*
import net.minecraft.world.World

interface NetworkWorker {
    suspend fun debugRender(part: TrackedStructurePart)
    val structure: TrackedStructure
    val world: World
    suspend fun refreshPositions(part: TrackedStructurePart) {
        if (part.tracker !is StructureTracker.Trackpoint) return
        val timeStart = System.currentTimeMillis()
        val timeEnd = System.currentTimeMillis()
        println("${this::class.java.simpleName}#refreshPositions: ${timeEnd - timeStart}ms")
    }

    suspend fun startUndoRecord(cause: PlayerData.UndoRecord.Cause)
    suspend fun stopUndoRecord()
    suspend fun paste(part: TrackedStructurePart)
    val coroutineDispatcher: CoroutineDispatcher
    suspend fun <T> execute(function: suspend CoroutineScope.() -> T): T =
        withContext(coroutineDispatcher, block = function)

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> async(function: suspend CoroutineScope.() -> T) = GlobalScope.async(coroutineDispatcher, block = function)

    @OptIn(DelicateCoroutinesApi::class)
    fun launch(function: suspend CoroutineScope.() -> Unit) = GlobalScope.launch(coroutineDispatcher, block = function)
    suspend fun trackpointUpdated(part: TrackedStructurePart) {}
}
