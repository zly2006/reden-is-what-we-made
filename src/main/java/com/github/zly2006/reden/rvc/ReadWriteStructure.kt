package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

abstract class ReadWriteStructure(override var name: String) : IStructure {
    val blocks = mutableMapOf<BlockPos, BlockState>()
    val blockEntities = mutableMapOf<BlockPos, NbtCompound>()
    override val entities = mutableListOf<NbtCompound>()
    override fun getBlockState(pos: BlockPos) = blocks[pos] ?: Blocks.AIR.defaultState!!
    override fun getBlockEntityData(pos: BlockPos) = blockEntities[pos]
    override fun getOrCreateBlockEntityData(pos: BlockPos) = blockEntities.getOrPut(pos) { NbtCompound() }
}