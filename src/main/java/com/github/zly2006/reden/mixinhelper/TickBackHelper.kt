package com.github.zly2006.reden.mixinhelper

import net.minecraft.util.math.ChunkPos
import java.nio.file.Path

object TickBackHelper {
    class TickBackup(
        val time: Long,
        val modifiedChunks: MutableSet<ChunkPos> = mutableSetOf(),
        val filePath: Path
    )
    var isTickStepping = false
    val backups = mutableMapOf<Long, TickBackup>()

}