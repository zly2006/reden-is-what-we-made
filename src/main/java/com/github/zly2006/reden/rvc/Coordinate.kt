package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos
import kotlin.math.max
import kotlin.math.min

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

inline fun <T : Coordinate, R> minMax(first: T, second: T, new: (Int, Int, Int) -> T, block: (T, T) -> R): R {
    val minX = min(first.x, second.x)
    val minY = min(first.y, second.y)
    val minZ = min(first.z, second.z)
    val maxX = max(first.x, second.x)
    val maxY = max(first.y, second.y)
    val maxZ = max(first.z, second.z)
    return block(new(minX, minY, minZ), new(maxX, maxY, maxZ))
}
