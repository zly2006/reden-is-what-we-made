package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text

class ServerRootStage(
    val server: MinecraftServer?
): TickStage(
    "server_root", null,
    Text.literal("Server Tick"),
    Text.literal("The root of a server tick")
) {
    override fun toString() = "Server RootStage"
}
