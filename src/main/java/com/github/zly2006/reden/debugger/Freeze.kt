package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.transformers.sendToAll
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import net.minecraft.util.Util

var disableWatchDog = false

fun tickPackets(server: MinecraftServer) {
    server.data().addStatus(GlobalStatus.FROZEN)
    // todo: tick command introduced in 1.20.2
    server.timeReference += 50 // for watchdog
    val globalStatus = GlobalStatus(server.data().status, NbtCompound().apply {
        putString("reason", "game-paused")
    })
    server.sendToAll(globalStatus)
    if (server is MinecraftDedicatedServer) {
        server.executeQueuedCommands()
    }
    if (server.runningTasks != 0 || !server.isOnThread) {
        error("")
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
    server.timeReference = Util.getMeasuringTimeMs()
}

fun unfreeze(server: MinecraftServer) {
    server.data().removeStatus(GlobalStatus.FROZEN)
    val globalStatus = GlobalStatus(server.data().status, NbtCompound().apply {
        putString("reason", "game-resumed")
    })
    server.sendToAll(globalStatus)
}
