package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.Person
import com.github.zly2006.reden.rvc.nbt.NbtDiff
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import java.util.*

class TrackedDiff(
    val parentIds: LongArray,
    /**
     * origin diff, = after - before
     */
    val originDiff: Array<BlockPos>,
    val xSize: Int,
    val ySize: Int,
    val zSize: Int,
    /**
     * should ensure that all keys are positive coordinates
     */
    val changedBlocks: Map<BlockPos, BlockState> = mutableMapOf(),
    /**
     * should ensure that all keys are positive coordinates
     */
    val changedBlockEntities: Map<BlockPos, NbtDiff> = mutableMapOf(),
    val removedBlockPoses: Set<BlockPos> = mutableSetOf(),
    val entities: Map<UUID, NbtDiff> = mutableMapOf(),
    val timestamp: Long,
    val author: Person
) {
    var id: Long = 0; private set

    fun getTrackingPoses(storage: TrackedDiffStorage): Set<BlockPos> =
        parentIds.map(storage::get).map { it.getTrackingPoses(storage) }.flatten().toMutableSet().apply {
            addAll(changedBlocks.keys)
            removeAll(removedBlockPoses)
        }

    fun getBlockState(storage: TrackedDiffStorage, pos: BlockPos): BlockState? {
        return when (parentIds.size) {
            0 -> changedBlocks[pos]
            1 -> changedBlocks[pos] ?: storage[parentIds[0]].getBlockState(storage, pos.subtract(originDiff[0]))
            else -> {

            }
        }
    }

    companion object {
        fun create(storage: TrackedDiffStorage, before: TrackedDiff, after: TrackingStructure, author: Person) {
            val originDiff = after.origin.toImmutable().subtract(before.originDiff[0])
            val trackingPoses = before.getTrackingPoses(storage).toMutableSet()
            fun getPosBefore(pos: BlockPos) = pos.subtract(originDiff)
            fun getPosAfter(pos: BlockPos) = pos.add(originDiff)

            val ret = TrackedDiff(
                parentIds = longArrayOf(before.id),
                originDiff = arrayOf(originDiff),
                author = author,
                xSize = after.xSize,
                ySize = after.ySize,
                zSize = after.zSize,
                timestamp = System.currentTimeMillis(),
                removedBlockPoses = before.getTrackingPoses(storage) - after.trackingPositions.map { it.subtract(after.origin) }.toSet(),
            )

            after.trackingPositions.filter { after.world.getBlockState(it) != before.getblockState(it.subtract(after.origin)) }
        }
    }
}