package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.access.WorldData.Companion.data
import net.minecraft.server.MinecraftServer

class ServerRootStage(
    val server: MinecraftServer
): TickStage("server_root") {
    init {
        server.worlds.forEach {
            val worldRootStage = WorldRootStage(it, this)
            children.add(worldRootStage)
            it.data().tickStage = worldRootStage
        }
        children.add(EndStage(this))
    }

    override fun reset() {
        tick() // reset stage iterators
        // todo: apply backups
    }
}
