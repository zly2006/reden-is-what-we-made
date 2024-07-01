package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.transformers.sendToAll
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import java.util.concurrent.locks.LockSupport

var disableWatchDog = false

fun tickPackets(server: MinecraftServer) {
    server.data.frozen = true
    server.tickStartTimeNanos += 50 // for watchdog
    if (server is MinecraftDedicatedServer) {
        server.executeQueuedCommands()
    }
    if (!server.isOnThread) {
        error("trying to tick packets out of server thread")
        // server already ticking packets:
        // server.runningTasks != 0
        // dont check this.
    }
    server.runTasks()
    // tick connections
    server.networkIo!!.connections.filter { !it.isOpen }.forEach {
        server.networkIo!!.connections.remove(it)
        it.handleDisconnection()
    }
    server.networkIo!!.connections.filter { it.isOpen }.forEach {
        it.tick()
    }

    // send block updates
    server.worlds.forEach {
        // fixme
        it.chunkManager.chunkLoadingManager.chunkHolders.values.forEach {
            val worldChunk = it.worldChunk
            if (worldChunk != null) {
                it.flushUpdates(worldChunk)
            }
        }
    }
    LockSupport.parkNanos("[Reden] IDLE", 100_000L) // to avoid 100% cpu usage
}

fun unfreeze(server: MinecraftServer) {
    server.data.frozen = false
    val globalStatus = GlobalStatus.packet(server.data.status, NbtCompound().apply {
        putString("reason", "game-resumed")
    })
    server.sendToAll(globalStatus)
}
