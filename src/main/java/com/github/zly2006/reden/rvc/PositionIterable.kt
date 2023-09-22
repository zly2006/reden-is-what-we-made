package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos

interface PositionIterable {
    /**
     * maybe this state is [net.minecraft.block.Blocks.AIR]
     *
     * All positions must be true for [isInArea].
     */
    val blockIterator: Iterator<BlockPos>
    fun isInArea(pos: BlockPos): Boolean
}