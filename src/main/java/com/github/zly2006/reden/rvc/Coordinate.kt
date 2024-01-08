package com.github.zly2006.reden.rvc

interface Coordinate {
    fun getForOrigin(origin: Coordinate): Coordinate
    val x: Int
    val y: Int
    val z: Int
    val isAbsolute: Boolean
        get() = this is AbsoluteCoordinate
}

fun Coordinate.getAbsolute(origin: Coordinate): AbsoluteCoordinate {
    var coordinate = this
    while (!coordinate.isAbsolute) {
        coordinate = coordinate.getForOrigin(origin)
    }
    return coordinate as AbsoluteCoordinate
}
