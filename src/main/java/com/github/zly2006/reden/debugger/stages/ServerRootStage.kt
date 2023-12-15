package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.server.MinecraftServer

class ServerRootStage(
    val server: MinecraftServer
): TickStage("server_root", null) {
    override fun toString() = "Server RootStage"
}
