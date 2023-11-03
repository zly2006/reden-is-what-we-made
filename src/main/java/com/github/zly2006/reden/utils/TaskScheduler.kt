package com.github.zly2006.reden.utils

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer

object TaskScheduler: ServerTickEvents.EndTick {
    val map = mutableMapOf<Int, MutableList<Runnable>>()
    var ticks = 0
    fun runLater(delay: Int, runnable: Runnable) {
        map.computeIfAbsent(ticks + delay) { mutableListOf() }
            .add(runnable)
    }

    override fun onEndTick(server: MinecraftServer) {
        ticks++
        map.remove(ticks)?.forEach { it.run() }
    }
}