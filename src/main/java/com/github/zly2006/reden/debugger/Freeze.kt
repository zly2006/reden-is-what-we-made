package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.transformers.sendToAll
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer

var disableWatchDog = false

fun tickPackets(server: MinecraftServer) {
    server.data().addStatus(GlobalStatus.FROZEN)
    val globalStatus = GlobalStatus(server.data().status, NbtCompound().apply {
        putString("reason", "game-paused")
    })
    server.sendToAll(globalStatus)
    server.runTasksTillTickEnd()
}

fun unfreeze(server: MinecraftServer) {
    server.data().removeStatus(GlobalStatus.FROZEN)
    val globalStatus = GlobalStatus(server.data().status, NbtCompound().apply {
        putString("reason", "game-resumed")
    })
    server.sendToAll(globalStatus)
}
