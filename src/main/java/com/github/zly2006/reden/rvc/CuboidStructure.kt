package com.github.zly2006.reden.rvc

abstract class CuboidStructure(
    name: String,
    override val xSize: Int,
    override val ySize: Int,
    override val zSize: Int
) : ReadWriteStructure(name) {

}
