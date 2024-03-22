package com.github.zly2006.reden.rvc.tracking.tracker

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.blockPos
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.util.math.BlockPos
import java.util.*

/**
 * track a structure's part, listen to block changes and update the positions of the tracked blocks.
 * provide a way to iterate through the tracked blocks.
 */
@Serializable
sealed class StructureTracker {
    /**
     * iterator of the tracked blocks
     *
     * if tracked was modified, this iterator should be updated by calling [refreshPositions]
     */
    abstract val blockIterator: Iterator<RelativeCoordinate>

    /**
     * cached origin of the structure part
     */
    @Transient
    protected var origin: BlockPos = BlockPos.ORIGIN

    @Serializable
    class Cuboid(
        var first: RelativeCoordinate,
        var second: RelativeCoordinate
    ) : StructureTracker() {
        override val blockIterator: Iterator<RelativeCoordinate>
            get() = sequence {
                val minX = kotlin.math.min(first.x, second.x)
                val minY = kotlin.math.min(first.y, second.y)
                val minZ = kotlin.math.min(first.z, second.z)
                val maxX = kotlin.math.max(first.x, second.x)
                val maxY = kotlin.math.max(first.y, second.y)
                val maxZ = kotlin.math.max(first.z, second.z)
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        for (z in minZ..maxZ) {
                            yield(RelativeCoordinate(x, y, z))
                        }
                    }
                }
        }.iterator()

        override fun isInArea(part: TrackedStructurePart, pos: RelativeCoordinate) =
            pos.blockPos(part.origin).run {
                x in first.x..second.x
                        && y in first.y..second.y
                        && z in first.z..second.z
            }

        override suspend fun refreshPositions(part: TrackedStructurePart) {
            updateOrigin(part)
        }

        override fun onBlockAdded(part: TrackedStructurePart, pos: BlockPos) {
            TODO("whether to expand the cuboid?")
        }

        override fun onBlockRemoved(part: TrackedStructurePart, pos: BlockPos) {}
    }

    @Serializable
    class Trackpoint(
        val trackpoints: MutableList<TrackPoint> = mutableListOf()
    ) : StructureTracker() {
        @Transient
        val trackpointMap = mutableMapOf<BlockPos, TrackPoint>()

        @Transient
        var cachedPositions = HashMap<BlockPos, TrackPoint>()

        @Transient
        var cachedIgnoredPositions = HashMap<BlockPos, TrackPoint>()

        override val blockIterator: Iterator<RelativeCoordinate>
            get() = cachedPositions.keys.asSequence().map {
            RelativeCoordinate.origin(origin).block(it)
        }.iterator()

        override fun updateOrigin(part: TrackedStructurePart) {
            origin = part.origin
            trackpoints.forEach { it.updateOrigin(part) }
            trackpointMap.clear()
            trackpoints.forEach { trackpointMap[it.pos] = it }
        }

        override fun onBlockAdded(part: TrackedStructurePart, pos: BlockPos) {
            part.dirty = true
            val trackpoint = TrackPredicate.entries.firstNotNullOfOrNull { predicate ->
                predicate.blocks(pos).firstOrNull { pos2 ->
                    trackpoints.firstOrNull { it.pos == pos2 }?.predicate == predicate &&
                            predicate.match(part.world, pos, pos2, TrackPredicate.TrackMode.TRACK, part)
                }?.let { trackpointMap[it] }
            } ?: return
            val readPos = mutableSetOf<BlockPos>()
            val queue = LinkedList<SpreadEntry>()
            queue.add(trackpoint)
            var maxElements = 8000
            while (queue.isNotEmpty() && maxElements > 0) {
                maxElements--
                val entry = queue.removeFirst()
                if (entry.pos in cachedIgnoredPositions || part.world.isAir(entry.pos)) continue
                entry.spreadAround(part.world, { newPos ->
                    if (readPos.add(newPos)) {
                        if (newPos in cachedPositions) return@spreadAround
                        if (!part.world.isAir(newPos)) {
                            cachedPositions[newPos] = trackpoint
                        }
                        queue.add(SpreadEntry(entry.predicate, trackpoint.mode).apply {
                            this.pos = newPos
                            this.part = part
                        })
                    }
                })
            }
        }

        override fun onBlockRemoved(part: TrackedStructurePart, pos: BlockPos) {
            part.dirty = true
            removeTrackpoint(pos)
            cachedPositions -= pos
        }

        fun removeTrackpoint(pos: BlockPos) {
            val existing = trackpointMap[pos] ?: return
            cachedPositions.entries.removeIf { it.value == existing }
            trackpoints -= existing
        }

        fun addTrackPoint(trackPoint: TrackPoint) {
            removeTrackpoint(trackPoint.pos)
            trackpointMap[trackPoint.pos] = trackPoint
            trackpoints.add(trackPoint)
        }

        override suspend fun refreshPositions(part: TrackedStructurePart) {
            cachedIgnoredPositions = hashMapOf()
            cachedPositions = hashMapOf()
            updateOrigin(part)
            val readPos = hashSetOf<BlockPos>()
            requireNotNull(part.structure.networkWorker).launch {
                trackpoints.filter { it.mode == TrackPredicate.TrackMode.IGNORE }.forEach { trackPoint ->
                    // first, add all blocks recursively
                    val queue = LinkedList<SpreadEntry>()
                    queue.add(trackPoint)
                    var maxElements = trackPoint.maxElements
                    while (queue.isNotEmpty() && maxElements > 0) {
                        maxElements--
                        val entry = queue.removeFirst()
                        if (entry.pos in cachedIgnoredPositions) continue
                        if (part.world.isAir(entry.pos)) {
                            continue
                        }
                        cachedIgnoredPositions[entry.pos] = trackPoint
                        entry.spreadAround(part.world, { newPos ->
                            if (readPos.add(newPos)) {
                                queue.add(SpreadEntry(entry.predicate, trackPoint.mode).apply {
                                    this.pos = newPos
                                    this.part = part
                                })
                            }
                        })
                    }
                }
                trackpoints.filter { it.mode == TrackPredicate.TrackMode.TRACK }.forEach { trackPoint ->
                    // first, add all blocks recursively
                    val queue = LinkedList<SpreadEntry>()
                    queue.add(trackPoint)
                    var maxElements = trackPoint.maxElements
                    while (queue.isNotEmpty() && maxElements > 0) {
                        maxElements--
                        val entry = queue.removeFirst()
                        if (entry.pos in cachedIgnoredPositions) continue
                        if (part.world.isAir(entry.pos)) {
                            continue
                        }
                        if (!trackPoint.pos.isWithinDistance(entry.pos, 200.0)) {
                            Reden.LOGGER.error("Track point ${trackPoint.pos} is too far away from ${entry.pos}")
                            continue
                        }
                        entry.spreadAround(part.world, { newPos ->
                            if (readPos.add(newPos)) {
                                if (newPos in cachedPositions) return@spreadAround
                                if (!part.world.isAir(newPos)) {
                                    cachedPositions[newPos] = trackPoint
                                }
                                queue.add(SpreadEntry(entry.predicate, trackPoint.mode).apply {
                                    this.pos = newPos
                                    this.part = part
                                })
                            }
                        })
                    }
                }
                part.structure.networkWorker?.trackpointUpdated(part)
            }
        }

        override fun isInArea(part: TrackedStructurePart, pos: RelativeCoordinate) =
            pos.blockPos(origin) in cachedPositions.keys
    }

    @Serializable
    data object Entire : StructureTracker() {
        override val blockIterator: Iterator<RelativeCoordinate>
            get() = TODO("Not yet implemented")

        override fun updateOrigin(part: TrackedStructurePart) = error("entire structure cannot be moved")
        override fun isInArea(part: TrackedStructurePart, pos: RelativeCoordinate) = true
        override suspend fun refreshPositions(part: TrackedStructurePart) {}
        override fun onBlockAdded(part: TrackedStructurePart, pos: BlockPos) {}
        override fun onBlockRemoved(part: TrackedStructurePart, pos: BlockPos) {}
    }

    /**
     * update the origin of the structure part
     * usually called by [TrackedStructurePart.createPlacement]
     */
    open fun updateOrigin(part: TrackedStructurePart) {
        origin = part.origin
    }

    abstract fun onBlockAdded(part: TrackedStructurePart, pos: BlockPos)
    abstract fun onBlockRemoved(part: TrackedStructurePart, pos: BlockPos)

    /**
     * check if the given position is in the area of the structure part
     * @param part the structure part
     * @param pos the position to check, relative to the structure part
     */
    abstract fun isInArea(part: TrackedStructurePart, pos: RelativeCoordinate): Boolean

    /**
     * refresh the positions of the tracked blocks, after called this method,
     * the [blockIterator] should return the correct positions
     */
    abstract suspend fun refreshPositions(part: TrackedStructurePart)
}
