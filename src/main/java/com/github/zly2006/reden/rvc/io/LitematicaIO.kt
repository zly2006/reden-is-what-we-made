package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.selection.AreaSelection
import fi.dy.masa.litematica.selection.Box
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

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
            ) ?: return
            boxes.mapIndexed { index, box ->
                val subRegionContainer = litematica.getSubRegionContainer(index.toString())!!
                val blockEntityMap = litematica.getBlockEntityMapForRegion(index.toString())!!
                val blockTicks = litematica.getScheduledBlockTicksForRegion(index.toString())!!
                val fluidTicks = litematica.getScheduledFluidTicksForRegion(index.toString())!!
                val entityInfos = litematica.getEntityListForRegion(index.toString())!!
                structure.blocks.filter { it.key in box }.forEach {
                    subRegionContainer.set(
                        it.key.x - box.minX,
                        it.key.y - box.minY,
                        it.key.z - box.minZ,
                        it.value
                    )
                }
                structure.blockEntities.keys.filter { it in box }.forEach { pos ->
                    blockEntityMap[pos] = structure.blockEntities[pos]
                }
                structure.entities.forEach {
                    entityInfos.add(
                        LitematicaSchematic.EntityInfo(
                            Vec3d.ZERO,
                            it.value
                        )
                    )
                }
                subRegionContainer
            }
        }
        else {
            val boxName = "RVC Structure"
            litematica = LitematicaSchematic.createEmptySchematic(
                AreaSelection().apply {
                    addSubRegionBox(
                        Box(
                            BlockPos.ORIGIN,
                            BlockPos(structure.xSize, structure.ySize, structure.zSize),
                            boxName
                        ),
                        false
                    )
                },
                "RVC"
            )
            val subRegionContainer = litematica.getSubRegionContainer(boxName)!!
            val blockEntityMap = litematica.getBlockEntityMapForRegion(boxName)!!
            val entityInfos = litematica.getEntityListForRegion(boxName)!!
            for (x in 0 until structure.xSize) {
                for (y in 0 until structure.ySize) {
                    for (z in 0 until structure.zSize) {
                        val pos = BlockPos(x, y, z)
                        subRegionContainer.set(x, y, z, structure.getBlockState(pos))
                        val be = structure.getBlockEntityData(pos)
                        if (be != null) {
                            blockEntityMap[pos] = be
                        }
                    }
                }
            }
            structure.entities.forEach {
                entityInfos.add(
                    LitematicaSchematic.EntityInfo(
                        Vec3d.ZERO,
                        it.value
                    )
                )
            }
        }
        litematica.metadata.description = "Reden Exported to Litematica"
        litematica.metadata.name = structure.name
        litematica.metadata.timeCreated = System.currentTimeMillis()
        litematica.writeToFile(
            path.toFile(),
            "Reden-Exported-${litematica.metadata.name}",
            true,
        )
    }

    override fun load(path: Path, structure: IWritableStructure) = throw UnsupportedOperationException()
}

private val BlockBox.minPos: BlockPos
    get() = BlockPos(minX, minY, minZ)
private val BlockBox.maxPos: BlockPos
    get() = BlockPos(maxX, maxY, maxZ)
