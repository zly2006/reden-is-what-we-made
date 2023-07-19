package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class TrackedDiff(
    val parent: IPlacement,
    val origin: BlockPos,
    val xSize: Int,
    val ySize: Int,
    val zSize: Int,
    val blocks: MutableMap<BlockPos, BlockState> = mutableMapOf(),
    val blockEntities: MutableMap<BlockPos, NbtCompound> = mutableMapOf(),
    val entities: MutableList<NbtCompound> = mutableListOf()
) {
    companion object {
        fun create(before: TrackingStructure, after: TrackingStructure) {
            val originDiff = after.origin.toImmutable().subtract(before.origin)
            val xSizeDiff = after.xSize - before.xSize
            val ySizeDiff = after.ySize - before.ySize
            val zSizeDiff = after.zSize - before.zSize
            fun getPosBefore(pos: BlockPos) = pos.subtract(originDiff)
        }
    }
}