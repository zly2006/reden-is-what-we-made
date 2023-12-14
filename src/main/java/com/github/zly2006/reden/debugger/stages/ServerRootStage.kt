package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.server.MinecraftServer
import java.util.function.BooleanSupplier

class ServerRootStage(
    val server: MinecraftServer
): TickStage("server_root", null) {
    lateinit var shouldKeepTicking: BooleanSupplier
    override fun tick() {
        super.tick()
        server.worlds.forEach {
            val worldRootStage = WorldRootStage(it, this, shouldKeepTicking)
            children.add(worldRootStage)
            it.data().tickStage = worldRootStage
        }
        children.add(GlobalNetworkStage(this))
        children.add(EndStage(this))

        val stageTree = server.data().tickStageTree
        stageTree.initRoot(this, true)
    }

    override fun reset() {
        this.tick() // reset stage iterators
        // todo: apply backups
    }

    fun yieldAndTick(shouldKeepTicking: BooleanSupplier) {
        server.tick(shouldKeepTicking)
    }

    override fun toString() = "Server RootStage"
}
