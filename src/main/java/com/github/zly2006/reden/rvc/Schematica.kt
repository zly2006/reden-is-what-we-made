package com.github.zly2006.reden.rvc

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path
import kotlin.io.path.extension

private object Names {
    const val MATERIALS = "Materials"
    const val FORMAT_STRUCTURE = "Structure"
}

val FORMATS = mutableMapOf<String, SchematicFormat>(Names.FORMAT_STRUCTURE to SchematicStructure())

abstract class SchematicFormat {
    abstract fun readFromNBT(tagCompound: NbtCompound): IStructure
    abstract fun writeToNBT(tagCompound: NbtCompound, schematic: IStructure): Boolean
}

class SchematicImpl(
    override var name: String,
    override val xSize: Int,
    override val ySize: Int,
    override val zSize: Int
) : IStructure {
    override fun save(path: Path) {
        if (path.extension.lowercase() != "schematic") throw IllegalArgumentException("path must be a schematic file")
    }

    override fun load(path: Path) {
        if (path.extension.lowercase() != "schematic") throw IllegalArgumentException("path must be a schematic file")
        val nbt = NbtIo.readCompressed(path.toFile())
        val format =
            if (nbt.contains(Names.MATERIALS)) nbt.getString(Names.MATERIALS)
            else Names.FORMAT_STRUCTURE

    }

    override fun isInArea(pos: BlockPos): Boolean {
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

    override fun getOrCreateBlockEntityData(pos: BlockPos): NbtCompound {
        TODO("Not yet implemented")
    }

    override val entities: List<NbtCompound>
        get() = TODO("Not yet implemented")
}

class SchematicStructure: SchematicFormat() {
    override fun readFromNBT(tagCompound: NbtCompound): IStructure {
        TODO("Not yet implemented")
    }

    override fun writeToNBT(tagCompound: NbtCompound, schematic: IStructure): Boolean {
        TODO("Not yet implemented")
    }
}