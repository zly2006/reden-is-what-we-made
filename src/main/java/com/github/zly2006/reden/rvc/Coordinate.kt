package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos

interface Coordinate {
    fun getForOrigin(origin: Coordinate): Coordinate
    val x: Int
    val y: Int
    val z: Int
    val isAbsolute: Boolean
        get() = this is AbsoluteCoordinate
}

/**
 * Get the absolute coordinate of this coordinate, relative to the given origin.
 */
fun Coordinate.getAbsolute(origin: Coordinate): AbsoluteCoordinate {
    var coordinate = this
    while (!coordinate.isAbsolute) {
        coordinate = coordinate.getForOrigin(origin)
    }
    return coordinate as AbsoluteCoordinate
}

/**
 * Get the [BlockPos] of this coordinate, relative to the given origin.
 */
fun Coordinate.blockPos(origin: BlockPos): BlockPos {
    var coordinate = this
    while (!coordinate.isAbsolute) {
        coordinate = coordinate.getForOrigin(AbsoluteCoordinate(origin))
    }
    return BlockPos(coordinate.x, coordinate.y, coordinate.z)
}
