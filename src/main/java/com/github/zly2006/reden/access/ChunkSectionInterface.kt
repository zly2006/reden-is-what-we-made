package com.github.zly2006.reden.access

import net.minecraft.util.math.BlockPos

@Suppress("INAPPLICABLE_JVM_NAME")
interface ChunkSectionInterface {
    @JvmName("getModifyTime\$reden")
    fun getModifyTime(pos: BlockPos): Int

    @JvmName("setModifyTime\$reden")
    fun setModifyTime(pos: BlockPos, time: Int)
}
