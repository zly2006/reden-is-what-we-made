package com.github.zly2006.reden.debugger.storage

import net.minecraft.world.World

interface ResetStorage {
    fun clear()
    fun apply(world: World)
}