package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.*
import com.github.zly2006.reden.rvc.tracking.PlacementInfo
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructurePart
import com.github.zly2006.reden.rvc.tracking.WorldInfo
import com.github.zly2006.reden.rvc.tracking.tracker.StructureTracker
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

open class LitematicaIO : StructureIO {
    override fun save(path: Path, structure: IStructure) {
        val litematica: LitematicaSchematic
        if (!path.exists()) {
            path.createDirectories()
        }
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
            val subRegion = schematic.getSubRegionContainer(regionName)!!
            val blockEntityMap = schematic.getBlockEntityMapForRegion(regionName)!!
            val entityInfos = schematic.getEntityListForRegion(regionName)!!
            val basePos = schematic.getSubRegionPosition(regionName)!!
            if (structure is TrackedStructure) {
                structure.placementInfo = PlacementInfo(WorldInfo())
                structure.regions[regionName] = TrackedStructurePart(
                    regionName,
                    structure,
                    StructureTracker.Cuboid(
                        RelativeCoordinate(basePos.x, basePos.y, basePos.z),
                        RelativeCoordinate(
                            basePos.x + subRegion.size.x,
                            basePos.y + subRegion.size.y,
                            basePos.z + subRegion.size.z
                        )
                    )
                ).apply {
                    createPlacement(structure.placementInfo!!.copy(origin = basePos))
                }
            }
            for (x in 0 until subRegion.size.x) {
                for (y in 0 until subRegion.size.y) {
                    for (z in 0 until subRegion.size.z) {
                        val pos = RelativeCoordinate(x + basePos.x, y + basePos.y, z + basePos.z)
                        if (subRegion.get(x, y, z).isAir) {
                            continue
                        }
                        structure.setBlockState(pos, subRegion.get(x, y, z))
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

    companion object Default : LitematicaIO() {
    }
}

private val BlockBox.minPos: BlockPos
    get() = BlockPos(minX, minY, minZ)
private val BlockBox.maxPos: BlockPos
    get() = BlockPos(maxX, maxY, maxZ)
