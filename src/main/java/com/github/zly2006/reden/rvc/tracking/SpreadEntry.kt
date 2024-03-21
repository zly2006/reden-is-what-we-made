package com.github.zly2006.reden.rvc.tracking

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

@Serializable
open class SpreadEntry(
    val predicate: TrackPredicate,
    val mode: TrackPredicate.TrackMode,
) {
    @Transient
    lateinit var pos: BlockPos

    @Transient
    var part: TrackedStructurePart? = null
    fun spreadAround(
        world: World,
        successConsumer: (BlockPos) -> Unit,
        failConsumer: ((BlockPos) -> Unit)? = null
    ) {
        val structurePart = requireNotNull(part)
        predicate.blocks(pos).forEach {
            if (predicate.match(world, it, pos, mode, structurePart)) {
                successConsumer(it)
            }
            else {
                failConsumer?.invoke(it)
            }
        }
    }
}
