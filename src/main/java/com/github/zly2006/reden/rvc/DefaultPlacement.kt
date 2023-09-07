package com.github.zly2006.reden.rvc

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DefaultPlacement(
    override val structure: IStructure,
    override val world: World,
    override val origin: BlockPos
): IPlacement {
    override var name: String = structure.name
    override var enabled: Boolean = true
}