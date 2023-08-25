package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.io.StructureIO
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.server.world.BlockEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.nio.file.Path
import java.util.UUID

object RvcFileIO: StructureIO {
    private fun rvcFile(name: String): String = "$name.rvc"
    private fun rvcHeader(name: String): String = "RVC; Version 1.0.0; Platform: MCMod/Reden; Data: $name\n"

    private fun writeRvcFile(path: Path, name: String, data: String) {
        path.resolve(rvcFile(name)).toFile().writeText(rvcHeader(name) + data)
    }

    private fun readRvcHeader(path: Path, name: String) {
        TODO("Not yet implemented")
    }

    private fun readRvcFile(path: Path, name: String): List<String> {
        if (path.resolve(rvcFile(name)).toFile().exists()) {
            readRvcHeader(path, name)
            val lines = path.resolve(rvcFile(name)).toFile().readLines()
            return lines.subList(1, lines.size)
        }
        return emptyList()
    }

    override fun save(path: Path, structure: IStructure) {
        // ================================ Check Saving Structure Type ================================
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }

        // ======================================== Save Blocks ========================================
        // public final val blocks: MutableMap<BlockPos, BlockState>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        var blocksStr = ""
        structure.blocks.forEach { (pos, state) ->
            blocksStr += "${pos.x},${pos.y},${pos.z},${
                NbtHelper.toNbtProviderString(NbtHelper.fromBlockState(state))
            }\n"
        }
        writeRvcFile(path, "blocks", blocksStr)

        // ==================================== Save Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        var blockEntitiesStr = ""
        structure.blockEntities.forEach { (pos, nbt) ->
            blockEntitiesStr += "${pos.x},${pos.y},${pos.z},${NbtHelper.toNbtProviderString(nbt)}\n"
        }
        writeRvcFile(path, "blockEntities", blockEntitiesStr)

        // ======================================= Save Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        var entitiesStr = ""
        structure.entities.forEach { (uuid, nbt) ->
            entitiesStr += "$uuid,${NbtHelper.toNbtProviderString(nbt)}\n"
        }
        writeRvcFile(path, "entities", entitiesStr)

        // ===================================== Save Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val trackPointsStr = structure.trackPoints.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.predicate},${it.mode}"
        }
        writeRvcFile(path, "trackPoints", trackPointsStr)

        // ===================================== Save Block Events =====================================
        // public final val blockEvents: MutableList<BlockEvent>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockEventsStr = structure.blockEvents.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.data},${Registries.BLOCK.getId(it.block)}"
        }
        writeRvcFile(path, "blockEvents", blockEventsStr)

        // ================================ Save Block Scheduled Ticks =================================
        // public final val blockScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockScheduledTicksStr = structure.blockScheduledTicks
            .joinToString("\n") { NbtHelper.toNbtProviderString(it) }
        writeRvcFile(path, "blockScheduledTicks", blockScheduledTicksStr)

        // ================================ Save Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val fluidScheduledTicksStr = structure.fluidScheduledTicks
            .joinToString("\n") { NbtHelper.toNbtProviderString(it) }
        writeRvcFile(path, "fluidScheduledTicks", fluidScheduledTicksStr)
    }

    override fun load(path: Path, structure: IWritableStructure) {
        // =============================== Check Loading Structure Type ================================
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }

        // ======================================== Load Blocks ========================================
        // public final val blocks: MutableMap<BlockPos, BlockState>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.blocks.clear()
        readRvcFile(path, "blocks").forEach {
            val split = it.split(",")
            structure.blocks[BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt())] = NbtHelper.toBlockState(
                Registries.BLOCK.readOnlyWrapper,
                NbtHelper.fromNbtProviderString(split[3])
            )
        }

        // ==================================== Load Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.blockEntities.clear()
        readRvcFile(path, "blockEntities").forEach {
            val split = it.split(",")
            structure.blockEntities[BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt())] = NbtHelper.fromNbtProviderString(
                split[3]
            )
        }

        // ======================================= Load Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.entities.clear()
        readRvcFile(path, "entities").forEach {
            val split = it.split(",")
            structure.entities[UUID.fromString(split[0])] = NbtHelper.fromNbtProviderString(split[1])
        }

        // ===================================== Load Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.trackPoints.clear()
        // TODO: TrackPoint deserialization needs to be implemented.

        // ===================================== Load Block Events =====================================
        // public final val blockEvents: MutableList<BlockEvent>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.blockEvents.clear()
        readRvcFile(path, "blockEvents").forEach {
            val split = it.split(",")
            structure.blockEvents.add(
                BlockEvent(
                    BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt()),
                    Registries.BLOCK.get(Identifier(split[5])),
                    split[3].toInt(),
                    split[4].toInt()
                )
            )
        }

        // ================================ Load Block Scheduled Ticks =================================
        // public final val blockScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.blockScheduledTicks.clear()
        readRvcFile(path, "blockScheduledTicks").forEach {
            structure.blockScheduledTicks.add(NbtHelper.fromNbtProviderString(it))
        }

        // ================================ Load Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.fluidScheduledTicks.clear()
        readRvcFile(path, "fluidScheduledTicks").forEach {
            structure.fluidScheduledTicks.add(NbtHelper.fromNbtProviderString(it))
        }
    }
}
