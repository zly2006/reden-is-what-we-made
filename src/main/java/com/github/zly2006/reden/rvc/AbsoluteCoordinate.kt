package com.github.zly2006.reden.rvc

class AbsoluteCoordinate(
    override val x: Int,
    override val y: Int,
    override val z: Int
): Coordinate {
    override fun getForOrigin(origin: Coordinate): Coordinate {
        TODO("Not yet implemented")
    }
}
