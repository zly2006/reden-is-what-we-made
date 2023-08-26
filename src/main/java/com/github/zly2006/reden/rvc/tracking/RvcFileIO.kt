package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.io.StructureIO
import com.github.zly2006.reden.rvc.tracking.reader.RvcReaderV1
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.server.world.BlockEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.nio.file.Path
import java.util.*

/**
 * Save and load [TrackedStructure]s into and from RVC files.
 */
object RvcFileIO: StructureIO {
    private fun rvcFile(name: String): String = "$name.rvc"
    private const val CURRENT_VERSION = "1.0.0"
    private val RVC_HEADER = IRvcFileReader.RvcHeader(
        mutableMapOf(
            "Version" to CURRENT_VERSION,
            "Platform" to "MCMod/Reden"
        )
    )
    private val VERSION_TO_READER = mapOf<String, (IRvcFileReader.RvcHeader) -> IRvcFileReader>(
        "1.0.0" to { RvcReaderV1(it) }
    )

    private fun writeRvcFile(path: Path, name: String, header: IRvcFileReader.RvcHeader, data: String) {
        path.resolve(rvcFile(name)).toFile().writeText("$header\n$data")
    }

    private fun readRvcHeader(header: String) {
        val header = IRvcFileReader.RvcHeader(header)
        val version = header.metadata["Version"] ?: CURRENT_VERSION
        val reader = VERSION_TO_READER[version] ?: throw IllegalArgumentException("Invalid RVC version")
        reader(header)
    }

    private fun readRvcFile(path: Path, name: String): List<String> {
        if (path.resolve(rvcFile(name)).toFile().exists()) {
            val lines = path.resolve(rvcFile(name)).toFile().readLines()
            readRvcHeader(lines[0])
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
        val blocksStr = structure.blocks.entries.joinToString("\n") { (pos, state) ->
            "${pos.x},${pos.y},${pos.z},${NbtHelper.toNbtProviderString(NbtHelper.fromBlockState(state))}"
        }
        writeRvcFile(path, "blocks", RVC_HEADER, blocksStr)

        // ==================================== Save Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        val blockEntitiesStr = structure.blockEntities.entries.joinToString("\n") { (pos, nbt) ->
            "${pos.x},${pos.y},${pos.z},${NbtHelper.toNbtProviderString(nbt)}"
        }
        writeRvcFile(path, "blockEntities", RVC_HEADER, blockEntitiesStr)

        // ======================================= Save Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        val entitiesStr = structure.entities.entries.joinToString("\n") { (uuid, nbt) ->
            "$uuid,${NbtHelper.toNbtProviderString(nbt)}"
        }
        writeRvcFile(path, "entities", RVC_HEADER, entitiesStr)

        // ===================================== Save Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val trackPointsStr = structure.trackPoints.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.predicate},${it.mode}"
        }
        writeRvcFile(path, "trackPoints", RVC_HEADER, trackPointsStr)

        // ===================================== Save Block Events =====================================
        // public final val blockEvents: MutableList<BlockEvent>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockEventsStr = structure.blockEvents.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.data},${Registries.BLOCK.getId(it.block)}"
        }
        writeRvcFile(path, "blockEvents", RVC_HEADER, blockEventsStr)

        // ================================ Save Block Scheduled Ticks =================================
        // public final val blockScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockScheduledTicksStr = structure.blockScheduledTicks
            .joinToString("\n") { NbtHelper.toNbtProviderString(it) }
        writeRvcFile(path, "blockScheduledTicks", RVC_HEADER, blockScheduledTicksStr)

        // ================================ Save Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val fluidScheduledTicksStr = structure.fluidScheduledTicks
            .joinToString("\n") { NbtHelper.toNbtProviderString(it) }
        writeRvcFile(path, "fluidScheduledTicks", RVC_HEADER, fluidScheduledTicksStr)
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
            val data = RvcDataReader(it, ",")
            val blockPos = BlockPos(data.readNext().toInt(), data.readNext().toInt(), data.readNext().toInt())
            val blockState = NbtHelper.toBlockState(
                Registries.BLOCK.readOnlyWrapper,
                NbtHelper.fromNbtProviderString(data.readGreedy())
            )
            structure.blocks[blockPos] = blockState
        }

        // ==================================== Load Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.blockEntities.clear()
        readRvcFile(path, "blockEntities").forEach {
            val data = RvcDataReader(it, ",")
            val blockPos = BlockPos(data.readNext().toInt(), data.readNext().toInt(), data.readNext().toInt())
            val nbt = NbtHelper.fromNbtProviderString(data.readGreedy())
            structure.blockEntities[blockPos] = nbt
        }

        // ======================================= Load Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure
        structure.entities.clear()
        readRvcFile(path, "entities").forEach {
            val data = RvcDataReader(it, ",")
            val uuid = UUID.fromString(data.readNext())
            val nbt = NbtHelper.fromNbtProviderString(data.readGreedy())
            structure.entities[uuid] = nbt
        }

        // ===================================== Load Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.trackPoints.clear()
        structure.trackPoints.addAll(readRvcFile(path, "trackPoints").map {
            val data = RvcDataReader(it, ",")
            val blockPos = BlockPos(data.readNext().toInt(), data.readNext().toInt(), data.readNext().toInt())
            val predicate = data.readNext()
            val mode = data.readNext()
            TrackedStructure.TrackPoint(
                blockPos,
                TrackedStructure.TrackPoint.TrackPredicate.valueOf(predicate),
                TrackedStructure.TrackPoint.TrackMode.valueOf(mode)
            )
        })

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
