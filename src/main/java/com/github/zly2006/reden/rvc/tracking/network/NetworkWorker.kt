package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.PlayerData
import com.github.zly2006.reden.rvc.tracking.*
import kotlinx.coroutines.*
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

interface NetworkWorker {
    suspend fun debugRender(part: TrackedStructurePart)
    val structure: TrackedStructure
    val world: World
    suspend fun refreshPositions(part: TrackedStructurePart) {
        val timeStart = System.currentTimeMillis()
        val readPos = hashSetOf<BlockPos>()
        if (part.tracker is StructureTracker.Cuboid) return
        if (part.tracker is StructureTracker.Trackpoint) {
            part.tracker.trackpoints.filter { it.mode == TrackPredicate.TrackMode.IGNORE }.forEach { trackPoint ->
                // first, add all blocks recursively
                val queue = LinkedList<SpreadEntry>()
                queue.add(trackPoint)
                var maxElements = trackPoint.maxElements
                while (queue.isNotEmpty() && maxElements > 0) {
                    maxElements--
                    val entry = queue.removeFirst()
                    if (entry.pos in part.cachedIgnoredPositions) continue
                    if (world.isAir(entry.pos)) {
                        continue
                    }
                    part.cachedIgnoredPositions[entry.pos] = trackPoint
                    entry.spreadAround(world, { newPos ->
                        if (readPos.add(newPos)) {
                            queue.add(SpreadEntry(newPos, entry.predicate, trackPoint.mode, part))
                        }
                    })
                }
            }
            part.tracker.trackpoints.filter { it.mode == TrackPredicate.TrackMode.TRACK }.forEach { trackPoint ->
                // first, add all blocks recursively
                val queue = LinkedList<SpreadEntry>()
                queue.add(trackPoint)
                var maxElements = trackPoint.maxElements
                while (queue.isNotEmpty() && maxElements > 0) {
                    maxElements--
                    val entry = queue.removeFirst()
                    if (entry.pos in part.cachedIgnoredPositions) continue
                    if (world.isAir(entry.pos)) {
                        continue
                    }
                    if (!trackPoint.pos.isWithinDistance(entry.pos, 200.0)) {
                        Reden.LOGGER.error("Track point ${trackPoint.pos} is too far away from ${entry.pos}")
                        continue
                    }
                    entry.spreadAround(world, { newPos ->
                        if (readPos.add(newPos)) {
                            if (newPos in part.cachedPositions) return@spreadAround
                            if (!world.isAir(newPos)) {
                                part.cachedPositions[newPos] = trackPoint
                            }
                            queue.add(SpreadEntry(newPos, entry.predicate, trackPoint.mode, part))
                        }
                    })
                }
            }
        }
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
}
