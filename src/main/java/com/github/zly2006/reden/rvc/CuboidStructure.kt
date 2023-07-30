package com.github.zly2006.reden.rvc

import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path

abstract class CuboidStructure(
    override var name: String,
    override val xSize: Int,
    override val ySize: Int,
    override val zSize: Int
) : IStructure {
    val palette = Object2IntRBTreeMap<Identifier>()
    override fun save(path: Path) {
        TODO("Not yet implemented")
    }

    override fun load(path: Path) {
        TODO("Not yet implemented")
    }

    override fun isInArea(pos: BlockPos): Boolean {
        TODO("Not yet implemented")
    }

    override fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound {
        TODO("Not yet implemented")
    }

    override fun createPlacement(world: World, origin: BlockPos): IPlacement {
        TODO("Not yet implemented")
    }

    override fun getBlockState(pos: BlockPos): BlockState {
        TODO("Not yet implemented")
    }

    override fun getBlockEntityData(pos: BlockPos): NbtCompound? {
        TODO("Not yet implemented")
    }
}