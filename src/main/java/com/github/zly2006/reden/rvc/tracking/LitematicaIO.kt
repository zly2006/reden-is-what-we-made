package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.io.StructureIO
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.selection.AreaSelection
import fi.dy.masa.litematica.selection.Box
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import java.io.File
import java.nio.file.Path

private val BlockBox.minPos: BlockPos
    get() {
        return BlockPos(minX, minY, minZ)
    }

private val BlockBox.maxPos: BlockPos
    get() {
        return BlockPos(maxX, maxY, maxZ)
    }

object LitematicaIO: StructureIO {
    override fun save(path: Path, structure: IStructure) {
        val litematica: LitematicaSchematic
        if (structure is TrackedStructure) {
            val boxes = structure.splitCuboids()
            litematica = LitematicaSchematic.createEmptySchematic(
                AreaSelection().apply {
                    boxes.mapIndexed { index, blockBox ->
                        addSubRegionBox(
                            Box(
                                blockBox.minPos,
                                blockBox.maxPos,
                                index.toString()
                            ),
                            false
                        )
                    }
                },
                "RVC"
            )
            litematica.writeToFile(
                File("schematics"),
                "RVC-Export-Test",
                true,
            )
            return
        }
        else {
            litematica = LitematicaSchematic.createEmptySchematic(
                AreaSelection().apply {
                    addSubRegionBox(
                        Box(
                            BlockPos.ORIGIN,
                            BlockPos(
                                structure.xSize,
                                structure.ySize,
                                structure.zSize
                            ),
                            "RVC Structure"
                        ),
                        false
                    )
                },
                "RVC"
            )
            TODO("Not yet implemented")
        }
    }

    override fun load(path: Path, structure: IWritableStructure) = throw UnsupportedOperationException()
}
