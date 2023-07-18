package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

interface IPlacement {
    var name: String
    var enabled: Boolean
    val structure: IStructure
    val world: World
    val origin: BlockPos
    fun clearArea()
    fun paste()
}
