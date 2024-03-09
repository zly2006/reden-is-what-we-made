package com.github.zly2006.reden.rvc.tracking.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.tracking.TrackPredicate
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

interface NetworkWorker {
    fun debugRender()
    val structure: TrackedStructure
    val world: World
    fun refreshPositions() {
        val timeStart = System.currentTimeMillis()
        val readPos = hashSetOf<BlockPos>()
        structure.trackPoints.asSequence().filter { it.mode == TrackPredicate.TrackMode.IGNORE }.forEach { trackPoint ->
            // first, add all blocks recursively
            val queue = LinkedList<TrackedStructure.SpreadEntry>()
            queue.add(trackPoint)
            var maxElements = trackPoint.maxElements
            while (queue.isNotEmpty() && maxElements > 0) {
                maxElements--
                val entry = queue.removeFirst()
                if (entry.pos in structure.cachedIgnoredPositions) continue
                if (world.isAir(entry.pos)) {
                    continue
                }
                structure.cachedIgnoredPositions[entry.pos] = trackPoint
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        queue.add(TrackedStructure.SpreadEntry(newPos, entry.predicate, trackPoint.mode, structure))
                    }
                })
            }
        }
        structure.trackPoints.asSequence().filter { it.mode == TrackPredicate.TrackMode.TRACK }.forEach { trackPoint ->
            // first, add all blocks recursively
            val queue = LinkedList<TrackedStructure.SpreadEntry>()
            queue.add(trackPoint)
            var maxElements = trackPoint.maxElements
            while (queue.isNotEmpty() && maxElements > 0) {
                maxElements--
                val entry = queue.removeFirst()
                if (entry.pos in structure.cachedIgnoredPositions) continue
                if (world.isAir(entry.pos)) {
                    continue
                }
                if (!trackPoint.pos.isWithinDistance(entry.pos, 200.0)) {
                    Reden.LOGGER.error("Track point ${trackPoint.pos} is too far away from ${entry.pos}")
                    continue
                }
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        if (newPos in structure.cachedPositions) return@spreadAround
                        if (!world.isAir(newPos)) {
                            structure.cachedPositions[newPos] = trackPoint
                        }
                        queue.add(TrackedStructure.SpreadEntry(newPos, entry.predicate, trackPoint.mode, structure))
                    }
                })
            }
        }
        val timeEnd = System.currentTimeMillis()
        println("${this::class.java.simpleName}#refreshPositions: ${timeEnd - timeStart}ms")
    }
}
