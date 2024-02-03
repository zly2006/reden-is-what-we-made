package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class CuboidStructure(
    name: String,
) : ReadWriteStructure(name), PositionIterable, SizeMutableStructure {
    override val blockIterator = object : Iterator<RelativeCoordinate> {
        private var x = 0
        private var y = 0
        private var z = 0
        override fun hasNext(): Boolean {
            return x < xSize && y < ySize && z < zSize
        }

        override fun next(): RelativeCoordinate {
            val pos = RelativeCoordinate(x, y, z)
            x++
            if (x >= xSize) {
                x = 0
                y++
                if (y >= ySize) {
                    y = 0
                    z++
                }
            }
            return pos
        }
    }

    override fun isInArea(pos: RelativeCoordinate): Boolean {
        return pos.x in 0 until xSize
                && pos.y in 0 until ySize
                && pos.z in 0 until zSize
    }

    override fun createPlacement(world: World, origin: BlockPos): IPlacement {
        TODO()
    }
}
