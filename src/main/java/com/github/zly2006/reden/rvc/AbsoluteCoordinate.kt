package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos

class AbsoluteCoordinate(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Coordinate {
    constructor(pos: BlockPos) : this(pos.x, pos.y, pos.z)

    override fun getForOrigin(origin: Coordinate) = this
}
