package com.github.zly2006.reden.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.util.math.BlockPos

@Environment(EnvType.CLIENT)
object StructureOutline {
    internal val set = mutableSetOf<BlockPos>()

    init {
    }
}