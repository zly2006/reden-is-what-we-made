package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.io.StructureIO
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.selection.AreaSelection
import fi.dy.masa.litematica.selection.Box
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

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
        if (!path.exists()) {
            path.createDirectories()
        }
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
            boxes.mapIndexed { index, box ->
                val subRegionContainer = litematica.getSubRegionContainer(index.toString())!!
                structure.cachedPositions.keys.filter { it in box }.forEach { pos ->
                    subRegionContainer.set(
                        pos.x - box.minX,
                        pos.y - box.minY,
                        pos.z - box.minZ,
                        structure.world.getBlockState(pos)
                    )
                }
                subRegionContainer
            }
            // todo
            litematica.writeToFile(
                path.toFile(),
                "RVC-Export-Test",
                true,
            )
            return
            // todo: finish non-tracked structures and move saving code to the end of the function
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
