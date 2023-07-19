package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.setBlockNoPP
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.IdList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path

class TrackingStructure: IStructure, IPlacement {
    var children = mutableListOf<IdList<TrackingStructure>>()
    override var name: String = ""
    override var enabled: Boolean = true
    override val structure: IStructure = this
    override lateinit var world: World
    override var xSize: Int = 0
    override var ySize: Int = 0
    override var zSize: Int = 0
    override val origin: BlockPos.Mutable = BlockPos.ORIGIN.mutableCopy()
    @JvmField val blocks = mutableMapOf<BlockPos, BlockState>()
    @JvmField val blockEntities = mutableMapOf<BlockPos, NbtCompound>()
    @JvmField val entities = mutableListOf<Entity>()
    override fun createPlacement(world: World, origin: BlockPos): TrackingStructure {
        if (world != this.world || origin != this.origin) {
            throw IllegalArgumentException("world and origin must be the same as the structure")
        }
        return this
    }

    override fun clearArea() {
        blocks.keys.forEach { world.removeBlock(it, false) }
    }

    override fun paste() {
        blocks.forEach { (pos, state) ->
            world.setBlockNoPP(pos, state, Block.NOTIFY_LISTENERS)
        }
    }

    override fun save(path: Path) {
        TODO("Not yet implemented")
    }

    override fun load(path: Path) {
        TODO("Not yet implemented")
    }

    override fun isInArea(pos: BlockPos) = pos in blocks
    override fun getBlockState(pos: BlockPos) = blocks[pos] ?: Blocks.AIR.defaultState!!
    override fun getBlockEntityData(pos: BlockPos) = blockEntities[pos]
    override fun getOrCreateBlockEntityData(pos: BlockPos) = blockEntities.getOrPut(pos) { NbtCompound() }
    override fun getEntities() =
        //world.getOtherEntities(null, Box(origin, origin.toImmutable().add(xSize, ySize, zSize)))
        entities.map { it.writeNbt(NbtCompound()) }
}
