package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path
import java.util.*

open class DummyStructure(
    final override var name: String,
    private var pXSize: Int = 0,
    private var pYSize: Int = 0,
    private var pZSize: Int = 0
) : IStructure {
    final override val xSize: Int
        get() = pXSize
    final override val ySize: Int
        get() = pYSize
    final override val zSize: Int
        get() = pZSize

    private var pBlockStateMap: MutableMap<BlockPos, BlockState> = mutableMapOf()
    private var pBlockEntitiesMap: MutableMap<BlockPos, BlockEntity> = mutableMapOf()
    private var pEntities: MutableList<Entity> = mutableListOf()

    // Dummy.
    override fun save(path: Path) {}

    // Dummy.
    override fun load(path: Path) {}

    final override fun isInArea(pos: BlockPos): Boolean =
        pos.x in 0 until xSize &&
        pos.y in 0 until ySize &&
        pos.z in 0 until zSize

    final override fun createPlacement(world: World, origin: BlockPos): IPlacement = object : IPlacement {
        override var name: String = this@DummyStructure.name
        override var enabled: Boolean = true
        override val structure: IStructure
            get() = this@DummyStructure
        override val world: World
            get() = world
        override val origin: BlockPos
            get() = origin

        override fun clearArea() { }

        override fun paste() { }
    }

    final override fun getBlockState(pos: BlockPos): BlockState = pBlockStateMap[pos] ?: Blocks.AIR.defaultState

    final override fun getBlockEntityData(pos: BlockPos): NbtCompound? = pBlockEntitiesMap[pos]?.createNbt()

    final override fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound = pBlockEntitiesMap[pos]?.createNbt() ?: run {
        val blockState = getBlockState(pos)
        return (blockState.block as BlockEntityProvider).createBlockEntity(pos, blockState)!!.createNbt()
    }

    override val entities: Map<UUID, NbtCompound>
        get() = emptyMap()
}
