package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.ReadWriteStructure
import com.github.zly2006.reden.rvc.io.RvcIO
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/*
 * TrackedStructure.kt
 * reden-is-what-we-made
 *
 * Created by Qian Qian "Cubik" on Tuesday Aug. 22.
 *
 * Copyright 2023 reden-is-what-we-made. Licensed under GNU General Public License v3.0.
 *
 */

class TrackedStructure (
    name: String
): ReadWriteStructure(name), IPlacement {
    override var xSize: Int = 0
    override var ySize: Int = 0
    override var zSize: Int = 0
    override var enabled: Boolean = true
    override val structure: IStructure = this
    override lateinit var world: World
    override val origin: BlockPos.Mutable = BlockPos.ORIGIN.mutableCopy()

    init {
        io = RvcIO
    }

    override fun isInArea(pos: BlockPos): Boolean {
        TODO("Not yet implemented")
    }

    override fun createPlacement(world: World, origin: BlockPos): TrackedStructure {
        TODO("Not yet implemented")
    }
}