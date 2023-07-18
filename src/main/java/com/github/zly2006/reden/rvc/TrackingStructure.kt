package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path

class TrackingStructure: IStructure {
    override var name: String = ""
    override val xSize: Int
        get() = TODO("Not yet implemented")
    override val ySize: Int
        get() = TODO("Not yet implemented")
    override val zSize: Int
        get() = TODO("Not yet implemented")

    override fun save(path: Path) {
        TODO("Not yet implemented")
    }

    override fun load(path: Path) {
        TODO("Not yet implemented")
    }

    override fun isInArea(pos: BlockPos): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPlacement(world: World, origin: BlockPos) = throw UnsupportedOperationException()

    override fun getBlockState(pos: BlockPos): BlockState {
        TODO("Not yet implemented")
    }

    override fun getBlockEntityData(pos: BlockPos): NbtCompound? {
        TODO("Not yet implemented")
    }

    override fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound {
        TODO("Not yet implemented")
    }

    override fun getEntities(): List<NbtCompound> {
        TODO("Not yet implemented")
    }
}