package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class CuboidStructure(
    name: String,
    override val xSize: Int,
    override val ySize: Int,
    override val zSize: Int
) : ReadWriteStructure(name), PositionIterable {
    override val blockIterator: Iterator<BlockPos> = object : Iterator<BlockPos> {
        private var x = 0
        private var y = 0
        private var z = 0
        override fun hasNext(): Boolean {
            return x < xSize && y < ySize && z < zSize
        }

        override fun next(): BlockPos {
            val pos = BlockPos(x, y, z)
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

    override fun isInArea(pos: BlockPos): Boolean {
        return pos.x in 0 until xSize
                && pos.y in 0 until ySize
                && pos.z in 0 until zSize
    }

    override fun createPlacement(world: World, origin: BlockPos): IPlacement {
        TODO()
    }
}
