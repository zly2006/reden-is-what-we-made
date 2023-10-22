package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.server.MinecraftServer
import java.util.function.BooleanSupplier

class ServerRootStage(
    val server: MinecraftServer
): TickStage("server_root", null) {
    var tickIndex = 0
    lateinit var shouldKeepTicking: BooleanSupplier
    override fun tick() {
        children.clear()
        server.worlds.forEach {
            val worldRootStage = WorldRootStage(it, this, shouldKeepTicking)
            children.add(worldRootStage)
            it.data().tickStage = worldRootStage
        }
        children.add(EndStage(this))

        // todo: initialize stage tree, start the game.
        server.data().tickStageTree.initRoot(this, true)
    }

    override fun reset() {
        tick() // reset stage iterators
        // todo: apply backups
    }

    fun yieldAndTick(shouldKeepTicking: BooleanSupplier) {
        server.tick(shouldKeepTicking)
    }
}
