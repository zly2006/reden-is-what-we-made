package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.rvc.io.StructureIO
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.NbtCompound
import java.nio.file.Path
import java.util.*

abstract class ReadWriteStructure(override var name: String) : IWritableStructure {
    protected var io: StructureIO? = null
    val blocks = mutableMapOf<RelativeCoordinate, BlockState>()
    val blockEntities = mutableMapOf<RelativeCoordinate, NbtCompound>()
    override val entities = mutableMapOf<UUID, NbtCompound>()
    override fun setBlockState(pos: RelativeCoordinate, state: BlockState) { blocks[pos] = state }
    override fun getBlockState(pos: RelativeCoordinate) = blocks[pos] ?: Blocks.AIR.defaultState!!
    override fun getBlockEntityData(pos: RelativeCoordinate) = blockEntities[pos]
    override fun getOrCreateBlockEntityData(pos: RelativeCoordinate) = blockEntities.getOrPut(pos) { NbtCompound() }
    override fun setBlockEntityData(pos: RelativeCoordinate, nbt: NbtCompound) {
        blockEntities[pos] = nbt
    }
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
            entities.putAll(another.entities)
        } else super.assign(another)
    }
}