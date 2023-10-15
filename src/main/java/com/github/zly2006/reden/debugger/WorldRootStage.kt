package com.github.zly2006.reden.debugger

import net.minecraft.server.world.ServerWorld

class WorldRootStage(
    val world: ServerWorld,
    parent: ServerRootStage
) : TickStage("world_root", parent = parent) {
    override fun tick() {
        TODO("Not yet implemented")
    }
}
