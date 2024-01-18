package com.github.zly2006.reden.rvc

interface PositionIterable {
    /**
     * maybe this state is [net.minecraft.block.Blocks.AIR]
     *
     * All positions must be true for [isInArea].
     */
    val blockIterator: Iterator<RelativeCoordinate>
    fun isInArea(pos: RelativeCoordinate): Boolean
}