package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.malilib.DO_ASSERTION_CHECKS
import com.github.zly2006.reden.rvc.Person
import com.github.zly2006.reden.rvc.nbt.DiffProvider
import com.github.zly2006.reden.rvc.nbt.DirectDiff
import com.github.zly2006.reden.rvc.nbt.NbtDiff
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import java.util.*
import java.util.function.Supplier

val diffProvider = DiffProvider { _, b ->
    DirectDiff(b)
}

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
    val changedBlocks: Map<BlockPos, BlockState> = emptyMap(),
    /**
     * should ensure that all keys are positive coordinates
     */
    val changedBlockEntities: Map<BlockPos, NbtDiff> = emptyMap(),
    val removedBlockPoses: Set<BlockPos> = emptySet(),
    val entities: Map<UUID, NbtDiff> = emptyMap(),
    val timestamp: Long,
    val author: Person
) {
    var id: Long = 0; private set

    fun getOrigin(storage: TrackedDiffStorage): BlockPos =
        if (parentIds.isEmpty()) BlockPos.ORIGIN
        else storage[parentIds[0]].getOrigin(storage).add(originDiff[0])

    fun getTrackingPoses(storage: TrackedDiffStorage): Set<BlockPos> =
        parentIds.map(storage::get).map { it.getTrackingPoses(storage) }.flatten().toMutableSet().apply {
            addAll(changedBlocks.keys)
            removeAll(removedBlockPoses)
        }

    fun getBlockState(storage: TrackedDiffStorage, pos: BlockPos): BlockState? {
        return when (parentIds.size) {
            0 -> changedBlocks[pos]
            1 -> changedBlocks[pos] ?: storage[parentIds[0]].getBlockState(storage, pos.subtract(originDiff[0]))
            else -> if (removedBlockPoses.contains(pos)) null
            else (changedBlocks[pos]) ?:
            parentIds.map(storage::get)
                .mapIndexed { index, it -> it.getBlockState(storage, pos.subtract(originDiff[index])) }
                .filterNotNull()
                .apply {
                    if (size > 1 && DO_ASSERTION_CHECKS.booleanValue) {
                        throw IllegalStateException("Found $size matching blocks. This diff may be broken")
                    }
                }.firstOrNull()
        }
    }

    companion object {
        fun create(storage: TrackedDiffStorage, before: TrackedDiff, after: TrackingStructure, author: Person): TrackedDiff {
            val beforeOrigin = before.getOrigin(storage)
            val originDiff = after.origin.toImmutable().subtract(beforeOrigin)
            val changedBlocks = mutableMapOf<BlockPos, BlockState>()
            val changedBlockEntities = mutableMapOf<BlockPos, NbtDiff>()

            val ret = TrackedDiff(
                parentIds = longArrayOf(before.id),
                originDiff = arrayOf(originDiff),
                author = author,
                changedBlocks = changedBlocks,
                changedBlockEntities = changedBlockEntities,
                xSize = after.xSize,
                ySize = after.ySize,
                zSize = after.zSize,
                timestamp = System.currentTimeMillis(),
                removedBlockPoses = before.getTrackingPoses(storage).map(originDiff::add).toSet() - after.trackingPositions.map { it.subtract(after.origin) }.toSet(),
            )

            for (it in after.trackingPositions) {
                val beforeState = before.getBlockState(storage, it.subtract(beforeOrigin))
                val afterState = after.world.getBlockState(it)
                if (beforeState != afterState) {
                    changedBlocks[it.subtract(after.origin)] = after.world.getBlockState(it)
                }
                val beforeData = before.getBlockEntityData(storage, it.subtract(beforeOrigin))
                val afterData = after.world.getBlockEntity(it)?.createNbtWithId()
                if (beforeData != afterData) {
                    changedBlockEntities[it.subtract(after.origin)] = diffProvider[beforeData, afterData]
                }
            }

            return ret
        }
    }
    object DummyDiff: NbtDiff {
        override fun apply(nbt: Supplier<NbtCompound>): NbtCompound = nbt.get()
        override fun combine(parent: NbtDiff): NbtDiff = parent
    }
    private fun getBlockEntityData(storage: TrackedDiffStorage, pos: BlockPos): NbtCompound? {
        fun applyDiff(diff: NbtDiff, supplier: Supplier<NbtCompound?>): NbtCompound? {
            return try {
                @Suppress("UNCHECKED_CAST")
                diff.apply(supplier as Supplier<NbtCompound>)
            } catch (_: NullPointerException) { null }
        }
        return when (parentIds.size) {
            0 -> null
            1 -> applyDiff(changedBlockEntities[pos] ?: DummyDiff){
                storage[parentIds[0]].getBlockEntityData(storage, pos.subtract(originDiff[0]))
            }
            else -> applyDiff(changedBlockEntities[pos] ?: DummyDiff) {
                parentIds.map(storage::get)
                    .mapIndexed { index, it -> it.getBlockEntityData(storage, pos.subtract(originDiff[index])) }
                    .filterNotNull()
                    .firstOrNull()
            }
        }
    }
}