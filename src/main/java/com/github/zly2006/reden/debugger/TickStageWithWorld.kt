package com.github.zly2006.reden.debugger

import net.minecraft.server.world.ServerWorld

interface TickStageWithWorld {
    val world: ServerWorld
}