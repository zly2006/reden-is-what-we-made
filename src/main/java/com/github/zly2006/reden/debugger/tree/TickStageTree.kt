package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.tickPackets
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.server
import net.minecraft.nbt.NbtCompound

class TickStageTree(
    val activeStages: MutableList<TickStage> = mutableListOf()
) {
    val activeStage get() = activeStages.lastOrNull()
    private val history = mutableListOf<TickStage>()

    private var stepOverUntil: TickStage? = null
    private var stepOverCallback: (() -> Unit)? = null
    private var steppingInto = false
    private var stepIntoCallback: (() -> Unit)? = null

    internal fun push(stage: TickStage) {
        require(stage.parent == activeStage) {
            "Stage $stage is not a child of $activeStage"
        }
        if (stage in activeStages) {
            Reden.LOGGER.error("Stage $stage is already active")
        }
        activeStages.add(stage)

        if (steppingInto) {
            steppingInto = false
            stepIntoCallback?.invoke()
            stepIntoCallback = null
            server.data().addStatus(GlobalStatus.FROZEN)
                .let {
                    GlobalStatus(it, NbtCompound().apply {
                        putString("reason", "step-into")
                    })
                }.let(server::sendToAll)
        }
        else if (stage == stepOverUntil) {
            stepOverUntil = null
            stepOverCallback?.invoke()
            stepOverCallback = null
            server.data().addStatus(GlobalStatus.FROZEN)
                .let {
                    GlobalStatus(it, NbtCompound().apply {
                        putString("reason", "step-over")
                    })
                }.let(server::sendToAll)
        }
        while (server.data().hasStatus(GlobalStatus.FROZEN) && server.isRunning) {
            tickPackets(server)
        }
        stage.preTick()
    }

    internal fun pop(): TickStage {
        val stage = activeStages.removeLast().also(history::add)
        stage.postTick()
        return stage
    }

    fun with(stage: TickStage, block: () -> Unit) {
        push(stage)
        block()
        pop()
    }

    fun stepOver(activeStage: TickStage, callback: () -> Unit): Boolean {
        stepOverUntil = activeStage
        stepOverCallback = callback
        server.data().removeStatus(GlobalStatus.FROZEN)
        return true
    }

    fun stepInto(callback: () -> Unit) {
        stepOverUntil = null
        steppingInto = true
        stepIntoCallback = callback
        server.data().removeStatus(GlobalStatus.FROZEN)
    }
}
