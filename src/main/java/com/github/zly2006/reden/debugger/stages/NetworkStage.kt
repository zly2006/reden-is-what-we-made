package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.network.ClientConnection
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.world.ServerWorld
import okhttp3.internal.toHexString

class NetworkStage(
    parent: GlobalNetworkStage,
    val connection: ClientConnection
): TickStage("network", parent), TickStageWithWorld {
    override fun tick() {
        parent as GlobalNetworkStage
        parent.io.tick()
    }

    override val world: ServerWorld
        get() = (connection.packetListener as ServerPlayNetworkHandler).player.serverWorld

    override fun toString(): String {
        return "NetworkTick/${connection.hashCode().toHexString()}"
    }
}