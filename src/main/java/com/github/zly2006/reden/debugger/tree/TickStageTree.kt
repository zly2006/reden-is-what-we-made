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
    private val stacktraces: MutableList<Array<StackTraceElement>?> = mutableListOf()

    private var stepOverUntil: TickStage? = null
    private var stepOverCallback: (() -> Unit)? = null
    private var steppingInto = false
    private var stepIntoCallback: (() -> Unit)? = null

    fun clear() {
        checkOnThread()
        Reden.LOGGER.debug("TickStageTree: clear()")
        activeStages.clear()
        history.clear()
        steppingInto = false
        stepOverUntil = null
        stepOverCallback = null
        stepIntoCallback = null
    }

    internal fun push(stage: TickStage) {
        checkOnThread()
        require(stage.parent == activeStage) {
            "Stage $stage is not a child of $activeStage"
        }
        if (stage in activeStages) {
            Reden.LOGGER.error("Stage $stage is already active")
        }
        activeStage?.children?.add(stage)
        activeStages.add(stage)
        //stacktraces.add(Thread.getAllStackTraces()[Thread.currentThread()])
        Reden.LOGGER.debug("TickStageTree: [{}] push {}", activeStages.size, stage)

        if (steppingInto) {
            steppingInto = false
            stepIntoCallback?.invoke()
            stepIntoCallback = null
            Reden.LOGGER.debug("TickStageTree: step into")
            server.data.frozen = true
            GlobalStatus(server.data.status, NbtCompound().apply {
                putString("reason", "step-into")
            }).let(server::sendToAll)

            while (server.data.frozen && server.isRunning) {
                tickPackets(server)
            }
        }
        Reden.LOGGER.debug("TickStageTree: preTick {}", stage)
        stage.preTick()
    }

    private fun checkOnThread() {
        if (!server.isOnThread) error("Calling tick stage tree off thread.")
    }

    internal fun pop(): TickStage {
        checkOnThread()

        val stage = activeStages.removeLast().also(history::add)
        stacktraces.removeLastOrNull()
        Reden.LOGGER.debug("TickStageTree: [{}] pop {}", activeStages.size, stage)
        stage.postTick()
        if (stage == stepOverUntil) {
            Reden.LOGGER.debug("stage == stepOverUntil")
            stepOverUntil = null
            stepOverCallback?.invoke()
            stepOverCallback = null
            server.data.addStatus(GlobalStatus.FROZEN)
                .let {
                    GlobalStatus(it, NbtCompound().apply {
                        putString("reason", "step-over")
                    })
                }.let(server::sendToAll)
            while (server.data.frozen && server.isRunning) {
                tickPackets(server)
            }
        }
        Reden.LOGGER.debug("TickStageTree: preTick {}", stage)
        return stage
    }

    fun with(stage: TickStage, block: () -> Unit) {
        try {
            push(stage)
            block()
        } catch (e: Exception) {
            Reden.LOGGER.error("Exception in stage $stage", e)
            Reden.LOGGER.error("Active stages:")
            for (tickStage in activeStages) {
                Reden.LOGGER.error("  $tickStage")
            }
        } finally {
            pop()
        }
    }

    fun stepOver(activeStage: TickStage, callback: () -> Unit): Boolean {
        stepOverUntil = activeStage
        stepOverCallback = callback
        server.data.frozen = false
        return true
    }

    fun stepInto(callback: () -> Unit) {
        stepOverUntil = null
        steppingInto = true
        stepIntoCallback = callback
        server.data.frozen = false
    }
}
