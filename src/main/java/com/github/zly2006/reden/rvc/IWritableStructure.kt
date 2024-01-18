package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import java.util.*

interface IWritableStructure: IStructure {
    fun setBlockState(pos: BlockPos, state: BlockState)
    fun setBlockEntityData(pos: BlockPos, nbt: NbtCompound)

    override val entities: MutableMap<UUID, NbtCompound>

    /**
     * copy block & entity data
     */
    fun assign(another: IStructure) {
        for (x in 0 until another.xSize)
            for (y in 0 until another.ySize)
                for (z in 0 until another.zSize) {
                    val pos = BlockPos(x, y, z)
                    setBlockState(pos, another.getBlockState(pos))
                    val anotherNbt = another.getBlockEntityData(pos)
                    if (anotherNbt != null) {
                        getOrCreateBlockEntityData(pos).copyFrom(anotherNbt)
                    }
                    else {
                        getOrCreateBlockEntityData(pos).entries.clear()
                    }
                }
        entities.clear()
        entities.putAll(another.entities)
    }
}

/**
 * grammar sugar to [IWritableStructure.assign]
 */
operator fun IWritableStructure.remAssign(another: IStructure) = assign(another)