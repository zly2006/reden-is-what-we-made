package com.github.zly2006.reden.rvc.tracking

import net.minecraft.util.math.BlockPos

sealed class StructureTracker {
    class Cuboid(
        val first: BlockPos,
        val second: BlockPos
    ) : StructureTracker() {
        override fun update(trackedStructurePart: TrackedStructurePart) {

        }
    }

    class Trackpoint(
        val trackpoints: MutableList<TrackPoint> = mutableListOf()
    ) : StructureTracker() {
        override fun update(part: TrackedStructurePart) {
            trackpoints.forEach { it.updateOrigin(part) }
        }
    }

    abstract fun update(trackedStructurePart: TrackedStructurePart)
}
