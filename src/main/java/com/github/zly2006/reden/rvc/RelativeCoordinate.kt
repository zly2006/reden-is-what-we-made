package com.github.zly2006.reden.rvc

class RelativeCoordinate(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Coordinate {
    override fun getForOrigin(origin: Coordinate): Coordinate {
        return AbsoluteCoordinate(x + origin.x, y + origin.y, z + origin.z)
    }
}
