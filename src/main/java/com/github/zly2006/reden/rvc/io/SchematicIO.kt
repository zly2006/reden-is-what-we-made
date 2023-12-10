package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.*
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtTagSizeTracker
import net.minecraft.registry.Registries
import net.minecraft.structure.StructureTemplate
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
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
    name: String,
    override val xSize: Int,
    override val ySize: Int,
    override val zSize: Int
) : ReadWriteStructure(name) {
    init {
        io = SchematicIO
    }
    override fun isInArea(pos: BlockPos): Boolean {
        return pos.x in 0 until xSize
                && pos.y in 0 until ySize
                && pos.z in 0 until zSize
    }

    override fun createPlacement(world: World, origin: BlockPos): IPlacement {
        return DefaultPlacement(this, world, origin)
    }
}

object SchematicIO: StructureIO {
    override fun save(path: Path, structure: IStructure) {
        if (path.extension.lowercase() != "schematic") throw IllegalArgumentException("path must be a schematic file")
        val format = FORMATS[Names.FORMAT_STRUCTURE]!!
        val nbt = NbtCompound()
        if (!format.writeToNBT(nbt, structure)) {
            throw Exception("Failed to save.")
        }
        NbtIo.writeCompressed(nbt, path)
    }

    override fun load(path: Path, structure: IWritableStructure) {
        if (path.extension.lowercase() != "schematic") throw IllegalArgumentException("path must be a schematic file")
        val nbt = NbtIo.readCompressed(path, NbtTagSizeTracker.ofUnlimitedBytes())
        val formatName = if (nbt.contains(Names.MATERIALS)) nbt.getString(Names.MATERIALS)
        else Names.FORMAT_STRUCTURE
        val format = FORMATS[formatName]
            ?: throw UnsupportedOperationException("Schematic format $formatName is not supported!")
        structure %= format.readFromNBT(nbt)
    }
}

class SchematicStructure: SchematicFormat() {
    override fun readFromNBT(tagCompound: NbtCompound): IStructure {
        val template = StructureTemplate()
        template.readNbt(Registries.BLOCK.readOnlyWrapper, tagCompound)
        val ret = SchematicImpl("", template.size.x, template.size.y, template.size.z)
        template.blockInfoLists.flatMap { it.all }.forEach {
            ret.setBlockState(it.pos, it.state)
            if (it.nbt != null) {
                ret.getOrCreateBlockEntityData(it.pos).copyFrom(it.nbt)
            }
        }
        return ret
    }

    override fun writeToNBT(tagCompound: NbtCompound, schematic: IStructure): Boolean {
        val template = StructureTemplate()
        template.author = "TEST ~ Reden ~"
        template.size = Vec3i(schematic.xSize, schematic.ySize, schematic.zSize)
        val list = mutableListOf<StructureTemplate.StructureBlockInfo>()
        for (x in 0 until schematic.xSize)
            for (y in 0 until schematic.ySize)
                for (z in 0 until schematic.zSize) {
                    val pos = BlockPos(x, y, z)
                    list.add(
                        StructureTemplate.StructureBlockInfo(
                            pos,
                            schematic.getBlockState(pos),
                            schematic.getBlockEntityData(pos)
                        )
                    )
                }
        template.blockInfoLists.clear()
        template.blockInfoLists.add(StructureTemplate.PalettedBlockInfoList(list))
        template.entities.clear()
        template.entities.addAll(schematic.entities.map {
            val posNbt = it.value.getList("Pos", NbtElement.DOUBLE_TYPE.toInt())
            val pos = Vec3d(posNbt.getDouble(0), posNbt.getDouble(1), posNbt.getDouble(2))
            StructureTemplate.StructureEntityInfo(
                pos,
                BlockPos.ofFloored(pos),
                it.value
            )
        })
        template.writeNbt(tagCompound)
        return true
    }
}