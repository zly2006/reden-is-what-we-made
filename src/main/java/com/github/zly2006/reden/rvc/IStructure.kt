package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path
import java.util.*

interface IStructure {
    var name: String
    val xSize: Int
    val ySize: Int
    val zSize: Int
    fun save(path: Path)
    fun load(path: Path)
    fun isInArea(pos: BlockPos): Boolean
    fun createPlacement(world: World, origin: BlockPos): IPlacement
    fun getBlockState(pos: BlockPos): BlockState
    fun getBlockEntityData(pos: BlockPos): NbtCompound?
    fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound
    val entities: Map<UUID, NbtCompound>
}
