package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.blockPos
import net.minecraft.util.math.BlockPos

class TrackPoint(
    private val relativeCoordinate: RelativeCoordinate,
    predicate: TrackPredicate,
    mode: TrackPredicate.TrackMode
) : SpreadEntry(BlockPos.ORIGIN, predicate, mode, null) {
    val maxElements: Int
        get() = when (mode) {
            TrackPredicate.TrackMode.IGNORE -> 5000
            TrackPredicate.TrackMode.TRACK -> 8000
            TrackPredicate.TrackMode.NOOP -> 0
        }

    fun updateOrigin(structure: TrackedStructurePart) {
        this.structure = structure
        pos = relativeCoordinate.blockPos(structure.origin)
    }

    override fun toString(): String {
        return "TrackPoint(${pos.toShortString()}, $predicate, $mode)"
    }
}
