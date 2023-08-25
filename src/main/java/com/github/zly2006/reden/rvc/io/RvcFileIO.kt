package com.github.zly2006.reden.rvc.io

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.registry.Registries
import net.minecraft.server.world.BlockEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.nio.file.Path

object RvcFileIO: StructureIO {
    private fun rvcFile(name: String) = "$name.rvc"

    private fun writeRvcHeader(path: Path, name: String) {
        path.resolve(rvcFile(name)).toFile().writeText(
            "RVC; Version 1.0.0; Platform: MCMod/Reden; Data: $name\n"
        )
    }

    private fun writeRvcFile(path: Path, name: String, data: String) {
        writeRvcHeader(path, name)
        path.resolve(rvcFile(name)).toFile().writeText(data)
    }

    private fun readRvcHeader(path: Path, name: String) {
        TODO("Not yet implemented")
    }

    private fun readRvcFile(path: Path, name: String): List<String> {
        readRvcHeader(path, name)
        if (path.resolve(rvcFile("blockEvents")).toFile().exists()) {
            return path.resolve(rvcFile(name)).toFile().readLines()
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

        // ==================================== Save Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure

        // ======================================= Save Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure

        // ===================================== Save Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val trackPointStr = structure.trackPoints.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.predicate},${it.mode}"
        }
        writeRvcFile(path, "trackPoints", trackPointStr)

        // ===================================== Save Block Events =====================================
        // public final val blockEvents: MutableList<BlockEvent>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockEventStr = structure.blockEvents.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.data},${Registries.BLOCK.getId(it.block)}"
        }
        writeRvcFile(path, "blockEvents", blockEventStr)

        // ================================ Save Block Scheduled Ticks =================================
        // public final val blockScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockScheduledTickStr = structure.blockScheduledTicks.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.delay},${it.priority.index}"
        }
        writeRvcFile(path, "blockScheduledTicks", blockScheduledTickStr)

        // ================================ Save Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val fluidScheduledTickStr = structure.fluidScheduledTicks.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.delay},${it.priority.index}"
        }
        writeRvcFile(path, "fluidScheduledTicks", fluidScheduledTickStr)
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

        // ==================================== Load Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.blockEntities.clear()

        // ======================================= Load Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.entities.clear()

        // ===================================== Load Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.trackPoints.clear()

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

        // ================================ Load Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.fluidScheduledTicks.clear()
    }
}
