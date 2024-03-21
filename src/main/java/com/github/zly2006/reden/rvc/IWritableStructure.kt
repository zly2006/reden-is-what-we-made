package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import java.util.*

interface IWritableStructure: IStructure {
    fun setBlockState(pos: RelativeCoordinate, state: BlockState)
    fun setBlockEntityData(pos: RelativeCoordinate, nbt: NbtCompound)
    override fun getOrCreateBlockEntityData(pos: RelativeCoordinate): NbtCompound {
        getBlockEntityData(pos)?.let { return it }
        val nbt = NbtCompound()
        setBlockEntityData(pos, nbt)
        return nbt
    }

    override val entities: MutableMap<UUID, NbtCompound>

    /**
     * copy block & entity data
     */
    fun assign(another: IStructure) {
        for (x in 0 until another.xSize)
            for (y in 0 until another.ySize)
                for (z in 0 until another.zSize) {
                    val pos = RelativeCoordinate(x, y, z)
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
