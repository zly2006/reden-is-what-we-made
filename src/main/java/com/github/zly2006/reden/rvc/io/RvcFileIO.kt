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
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }
        val blockEventStr = structure.blockEvents.joinToString("\n") {
            "${it.pos.x},${it.pos.y},${it.pos.z},${it.type},${it.data},${Registries.BLOCK.getId(it.block)}"
        }
        path.resolve(rvcFile("blockEvents")).toFile().writeText(blockEventStr)
    }

    override fun load(path: Path, structure: IWritableStructure) {
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }
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
    }
}
