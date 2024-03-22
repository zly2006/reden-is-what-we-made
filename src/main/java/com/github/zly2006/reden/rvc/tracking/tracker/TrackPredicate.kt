package com.github.zly2006.reden.rvc.tracking.tracker

import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import kotlinx.serialization.Serializable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Serializable
enum class TrackPredicate(val distance: Int, val same: Boolean) {
    Same(2, true),
    Near(1, false),
    QC(2, false) {
        override fun match(
            world: World,
            pos1: BlockPos,
            pos2: BlockPos,
            mode: TrackMode,
            structure: TrackedStructurePart
        ): Boolean {
            if (!super.match(world, pos1, pos2, mode, structure)) return false
            if (mode == TrackMode.TRACK && pos1.getSquaredDistance(pos2) >= 3.9) { // Euclidean distance = 2
                // check if the middle block is air.
                // if not, then there may be some block ignored between [pos1] and [pos2]
                // in that case we should not track this block
                return world.isAir(BlockPos((pos1.x + pos2.x) / 2, (pos1.y + pos2.y) / 2, (pos1.z + pos2.z) / 2))
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
        structure: TrackedStructurePart
    ): Boolean {
        // Note: we have checked this condition in [spreadAround]
        if (pos1.getManhattanDistance(pos2) > this.distance) {
            return false
        }
        if (this.same && world.getBlockState(pos1).block != world.getBlockState(pos2).block) {
            return false
        }
        return true
    }

    @Serializable
    enum class TrackMode {
        NOOP,
        TRACK,
        IGNORE;

        fun isTrack(): Boolean {
            return this == TRACK
        }
    }

    fun blocks(pos: BlockPos) = sequence {
        val x = pos.x
        val y = pos.y
        val z = pos.z
        val deltaRange = -distance..distance
        for (dx in deltaRange) {
            for (dy in deltaRange) {
                for (dz in deltaRange) {
                    val pos2 = BlockPos(x + dx, y + dy, z + dz)
                    if (pos2.getManhattanDistance(pos) > distance) continue
                    yield(pos2)
                }
            }
        }
    }
}
