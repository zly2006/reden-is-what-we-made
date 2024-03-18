package com.github.zly2006.reden.rvc.tracking

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class SpreadEntry(
    var pos: BlockPos,
    val predicate: TrackPredicate,
    val mode: TrackPredicate.TrackMode,
    var structure: TrackedStructurePart?
) {
    fun spreadAround(
        world: World,
        successConsumer: (BlockPos) -> Unit,
        failConsumer: ((BlockPos) -> Unit)? = null
    ) {
        val x = pos.x
        val y = pos.y
        val z = pos.z
        val deltaRange = -predicate.distance..predicate.distance
        for (dx in deltaRange) {
            for (dy in deltaRange) {
                for (dz in deltaRange) {
                    val pos = BlockPos(x + dx, y + dy, z + dz)
                    if (pos.getManhattanDistance(this.pos) > predicate.distance) continue
                    if (predicate.match(world, this.pos, pos, mode, structure!!)) {
                        successConsumer(pos)
                    }
                    else {
                        failConsumer?.invoke(pos)
                    }
                }
            }
        }
    }
}
