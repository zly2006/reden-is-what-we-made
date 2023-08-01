package com.github.zly2006.reden.rvc

import com.github.zly2006.reden.utils.setBlockNoPP
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IPlacement {
    var name: String
    var enabled: Boolean
    val structure: IStructure
    val world: World
    val origin: BlockPos
    fun clearArea() {
        val mutablePos = origin.mutableCopy()
        for (x in 0 until structure.xSize) {
            for (y in 0 until structure.ySize) {
                for (z in 0 until structure.zSize) {
                    mutablePos.set(origin.x + x, origin.y + y, origin.z + z)
                    if (structure.isInArea(mutablePos)) {
                        world.removeBlock(mutablePos, false)
                    }
                }
            }
        }
    }
    fun paste() {
        val mutablePos = origin.mutableCopy()
        for (x in 0 until structure.xSize) {
            for (y in 0 until structure.ySize) {
                for (z in 0 until structure.zSize) {
                    mutablePos.set(origin.x + x, origin.y + y, origin.z + z)
                    if (structure.isInArea(mutablePos)) {
                        world.setBlockNoPP(mutablePos, structure.getBlockState(mutablePos), Block.NOTIFY_LISTENERS)
                    }
                }
            }
        }
    }
}
