package com.github.zly2006.reden.debugger

import net.minecraft.server.world.ServerWorld

/**
 * Tick stage, but you can get a server world from it.
 *
 * Note that this class is used both on server and client side.
 */
interface TickStageWithWorld {
    /**
     * is `null` in client side.
     */
    val world: ServerWorld?
}
