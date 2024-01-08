package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos

class RelativeCoordinate(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Coordinate {
    override fun getForOrigin(origin: Coordinate): Coordinate {
        return BlockPos(x + origin.x, y + origin.y, z + origin.z) as Coordinate
    }
}
