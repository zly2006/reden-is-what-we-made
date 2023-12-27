package com.github.zly2006.reden.rvc.tracking

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

enum class TrackPredicate(val distance: Int, val same: Boolean) {
    Same(2, true),
    Near(1, false),
    QC(2, false) {
        override fun match(
            world: World,
            pos1: BlockPos,
            pos2: BlockPos,
            mode: TrackMode,
            structure: TrackedStructure
        ): Boolean {
            if (!super.match(world, pos1, pos2, mode, structure)) return false
            if (mode == TrackMode.TRACK && pos1.getManhattanDistance(pos2) == 2) {
                val center = pos1.add(pos2).mutableCopy().apply {
                    x /= 2
                    y /= 2
                    z /= 2
                }
                if (center in structure.cachedIgnoredPositions) {
                    return false
                }
            }
            return true
        }
    },
    Far(3, false);

    open fun match(
        world: World,
        pos1: BlockPos,
        pos2: BlockPos,
        mode: TrackMode,
        structure: TrackedStructure
    ): Boolean {
        if (pos1.getManhattanDistance(pos2) > this.distance) {
            return false
        }
        if (this.same && world.getBlockState(pos1).block != world.getBlockState(pos2).block) {
            return false
        }
        return true
    }

    enum class TrackMode {
        NOOP,
        TRACK,
        IGNORE;

        fun isTrack(): Boolean {
            return this == TRACK
        }
    }
}
