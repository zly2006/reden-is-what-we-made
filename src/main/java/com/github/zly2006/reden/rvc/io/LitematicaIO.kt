package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.*
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.selection.AreaSelection
import fi.dy.masa.litematica.selection.Box
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name

open class LitematicaIO: StructureIO {
    override fun save(path: Path, structure: IStructure) {
        save(path, structure, false)
    }

    fun save(path: Path, structure: IStructure, multiBox: Boolean) {
        val litematica: LitematicaSchematic
        if (!path.exists()) {
            path.createDirectories()
        }
        if (structure is TrackedStructurePart && multiBox) {
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
                structure.blockScheduledTicks.addAll(blockTicks.map {
                    TrackedStructurePart.TickInfo.wrap(
                        it.value,
                        structure.world
                    )
                })
                structure.fluidScheduledTicks.addAll(fluidTicks.map {
                    TrackedStructurePart.TickInfo.wrap(
                        it.value,
                        structure.world
                    )
                })
                val entityInfos = litematica.getEntityListForRegion(index.toString())!!
                structure.blocks.filter { it.key.blockPos(BlockPos.ORIGIN) in box }.forEach {
                    subRegionContainer.set(
                        it.key.x - box.minX,
                        it.key.y - box.minY,
                        it.key.z - box.minZ,
                        it.value
                    )
                }
                structure.blockEntities.keys.filter { it.blockPos(BlockPos.ORIGIN) in box }.forEach { pos ->
                    blockEntityMap[pos.blockPos(BlockPos.ORIGIN)] = structure.blockEntities[pos]
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
            val structure = if (structure is TrackedStructurePart) {
                structure.asCuboid()
            } else structure
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
                        val pos = RelativeCoordinate(x, y, z)
                        val state = structure.getBlockState(pos)
                        subRegionContainer.set(x, y, z, state)
                        val be = structure.getBlockEntityData(pos)
                        if (be != null) {
                            blockEntityMap[pos.blockPos(BlockPos.ORIGIN)] = be
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
        litematica.subRegionCount
        litematica.writeToFile(
            path.toFile(),
            structure.name,
            true,
        )
    }

    override fun load(path: Path, structure: IWritableStructure) {
        val schematic = LitematicaSchematic.createFromFile(path.parent.toFile(), path.name)!!
        if (structure is SizeMutableStructure) {
            val size = schematic.metadata.enclosingSize
            structure.xSize = size.x
            structure.ySize = size.y
            structure.zSize = size.z
        }
        schematic.areas.keys.forEach { regionName ->
            val subRegionContainer = schematic.getSubRegionContainer(regionName)!!
            val blockEntityMap = schematic.getBlockEntityMapForRegion(regionName)!!
            val entityInfos = schematic.getEntityListForRegion(regionName)!!
            val basePos = schematic.getSubRegionPosition(regionName)!!
            for (x in 0 until subRegionContainer.size.x) {
                for (y in 0 until subRegionContainer.size.y) {
                    for (z in 0 until subRegionContainer.size.z) {
                        val pos = RelativeCoordinate(x + basePos.x, y + basePos.y, z + basePos.z)
                        if (subRegionContainer.get(x, y, z).isAir) {
                            continue
                        }
                        structure.setBlockState(pos, subRegionContainer.get(x, y, z))
                        val be = blockEntityMap[BlockPos(x, y, z)]
                        if (be != null) {
                            // Note: litematica may add some unnecessary data
                            be.remove("x")
                            be.remove("y")
                            be.remove("z")
                            // keep the id, some contraptions may need it such as the CCE suppressor
                            // be.remove("id")
                            structure.setBlockEntityData(pos, be)
                        }
                    }
                }
            }
            entityInfos.forEach {
                structure.entities[it.nbt.getUuid("UUID")] = it.nbt
            }
        }
    }

    companion object Default: LitematicaIO() {
    }
}

private val BlockBox.minPos: BlockPos
    get() = BlockPos(minX, minY, minZ)
private val BlockBox.maxPos: BlockPos
    get() = BlockPos(maxX, maxY, maxZ)
