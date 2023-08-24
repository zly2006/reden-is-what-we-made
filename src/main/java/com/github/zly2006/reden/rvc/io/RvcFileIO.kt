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

        // ===================================== Save Block Events =====================================
        // public final val blockEvents: MutableList<BlockEvent>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        val blockEventStr = structure.blockEvents.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.data},${Registries.BLOCK.getId(it.block)}"
        }
        path.resolve(rvcFile("blockEvents")).toFile().writeText(blockEventStr)

        // ================================ Save Block Scheduled Ticks =================================
        // public final val blockScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure

        // ================================ Save Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
    }

    override fun load(path: Path, structure: IWritableStructure) {
        // =============================== Check Loading Structure Type ================================
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }

        // ======================================== Load Blocks ========================================
        // public final val blocks: MutableMap<BlockPos, BlockState>
        // com.github.zly2006.reden.rvc.ReadWriteStructure

        // ==================================== Load Block Entities ====================================
        // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure

        // ======================================= Load Entities =======================================
        // public open val entities: MutableMap<UUID, NbtCompound>
        // com.github.zly2006.reden.rvc.ReadWriteStructure

        // ===================================== Load Track Points =====================================
        // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure

        // ===================================== Load Block Events =====================================
        // public final val blockEvents: MutableList<BlockEvent>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
        structure.blockEvents.clear()
        if (path.resolve(rvcFile("blockEvents")).toFile().exists()) {
            path.resolve(rvcFile("blockEvents")).toFile().readLines().forEach {
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
        }

        // ================================ Load Block Scheduled Ticks =================================
        // public final val blockScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure

        // ================================ Load Fluid Scheduled Ticks =================================
        // public final val fluidScheduledTicks: MutableList<Tick<*>>
        // com.github.zly2006.reden.rvc.tracking.TrackedStructure
    }
}
