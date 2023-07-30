package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.rvc.io.StructureIO
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import java.nio.file.Path

abstract class ReadWriteStructure(override var name: String) : IWritableStructure {
    protected var io: StructureIO? = null
    val blocks = mutableMapOf<BlockPos, BlockState>()
    val blockEntities = mutableMapOf<BlockPos, NbtCompound>()
    override val entities = mutableListOf<NbtCompound>()
    override fun setBlockState(pos: BlockPos, state: BlockState) { blocks[pos] = state }
    override fun getBlockState(pos: BlockPos) = blocks[pos] ?: Blocks.AIR.defaultState!!
    override fun getBlockEntityData(pos: BlockPos) = blockEntities[pos]
    override fun getOrCreateBlockEntityData(pos: BlockPos) = blockEntities.getOrPut(pos) { NbtCompound() }
    override fun save(path: Path) { io?.save(path, this) }
    override fun load(path: Path) { io?.load(path, this) }
    override fun assign(another: IStructure) {
        // 看见没，什么叫高效
        if (another is ReadWriteStructure) {
            blocks.clear()
            blocks.putAll(another.blocks)
            blockEntities.clear()
            blockEntities.putAll(another.blockEntities)
            entities.clear()
            entities.addAll(another.entities)
        } else super.assign(another)
    }
}