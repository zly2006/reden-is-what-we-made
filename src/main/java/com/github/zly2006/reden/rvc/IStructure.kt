package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.rvc.io.StructureIO
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path
import java.util.*

/**
 * A structure is a collection of blocks and entities that can be placed in the world.
 * Your building, contraption, or whatever you want to call it, is a structure.
 *
 * A structure is read-only by default, you may want to implement [IWritableStructure] to make it mutable.
 *
 * @property name The name of the structure, usually should be unique.
 */
interface IStructure {
    var name: String
    val xSize: Int
    val ySize: Int
    val zSize: Int
    /**
     * @see [StructureIO.save]
     */
    fun save(path: Path)
    /**
     * @see [StructureIO.load]
     */
    fun load(path: Path)
    fun isInArea(pos: BlockPos): Boolean
    fun createPlacement(world: World, origin: BlockPos): IPlacement
    fun getBlockState(pos: BlockPos): BlockState
    fun getBlockEntityData(pos: BlockPos): NbtCompound?
    fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound
    val entities: Map<UUID, NbtCompound>
}
