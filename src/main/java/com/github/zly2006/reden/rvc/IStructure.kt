package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.rvc.io.StructureIO
import com.github.zly2006.reden.rvc.tracking.PlacementInfo
import com.github.zly2006.reden.rvc.tracking.WorldInfo
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
    fun isInArea(pos: RelativeCoordinate): Boolean
    fun createPlacement(world: World, origin: BlockPos) =
        createPlacement(PlacementInfo(WorldInfo.of(world), origin))

    fun createPlacement(placementInfo: PlacementInfo): IPlacement
    fun getBlockState(pos: RelativeCoordinate): BlockState
    fun getBlockEntityData(pos: RelativeCoordinate): NbtCompound?
    fun getOrCreateBlockEntityData(pos: RelativeCoordinate): NbtCompound
    val entities: Map<UUID, NbtCompound>
}
