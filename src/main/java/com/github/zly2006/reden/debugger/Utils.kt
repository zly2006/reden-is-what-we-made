package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.Reden
import net.minecraft.network.ClientConnection
import net.minecraft.network.PacketCallbacks
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.crash.CrashException
import net.minecraft.util.crash.CrashReport

@Suppress("FunctionName")
private fun ClientConnection.reden_tickPacketsOnly() {
    sendQueuedPackets()
    channel?.flush()
    tick()
}

/**
 * Maybe you will want to call [MinecraftServer.runTasks] as well.
 */
fun MinecraftServer.tickPacketsIo() {
    networkIo?.run {
        synchronized(this.connections) {
            val toRemove = mutableListOf<ClientConnection>()
            connections.filter { !it.isChannelAbsent }.forEach { clientConnection ->
                if (clientConnection.isOpen) {
                    try {
                        clientConnection.reden_tickPacketsOnly()
                    } catch (ex: Exception) {
                        if (clientConnection.isLocal) {
                            throw CrashException(CrashReport.create(ex, "Ticking memory connection"))
                        }
                        Reden.LOGGER.warn("Ticking connection: Failed to handle packet for {}", clientConnection.getAddress(), ex)
                        val text = Text.literal("Internal server error")
                        clientConnection.send(DisconnectS2CPacket(text), PacketCallbacks.always { clientConnection.disconnect(text) })
                        clientConnection.disableAutoRead()
                    }
                } else {
                    toRemove.add(clientConnection)
                    clientConnection.handleDisconnection()
                }
            }
            toRemove.forEach(connections::remove)
        }
    }
}
