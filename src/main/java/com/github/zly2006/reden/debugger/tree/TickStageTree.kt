package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.access.TickStageTreeOwnerAccess
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.TickStageWorldProvider
import com.github.zly2006.reden.debugger.tickPackets
import com.github.zly2006.reden.utils.server
import net.minecraft.block.BlockState
import net.minecraft.server.world.BlockEvent
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.tick.OrderedTick

class TickStageTree(
    val activeStages: MutableList<TickStage> = mutableListOf()
) {
    val activeStage get() = activeStages.lastOrNull()

    /**
     * Stages that have been ticked.
     */
    private val history = mutableListOf<TickStage>()
    // only used for debugging, DO NOT use it in production!! it is very very slow
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
        if (steppingInto || stepOverUntil != null) {
            server.data.frozen = false
        }
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

        // Note: some network packets should not trigger step into
        if (steppingInto && stage !is TickStageWorldProvider) {
            steppingInto = false
            stepIntoCallback?.invoke()
            stepIntoCallback = null
            Reden.LOGGER.debug("TickStageTree: step into")
            server.data.freeze("step-into")
            while (server.data.frozen && server.isRunning) {
                tickPackets(server)
            }
        }
        Reden.LOGGER.debug("TickStageTree: preTick {}", stage)
        stage.status = TickStage.StageStatus.Pending
        stage.preTick()
    }

    private fun checkOnThread() {
        if (!server.isOnThread) error("Calling tick stage tree off thread.")
    }

    fun pop(clazz: Class<out TickStage>) {
        val stage = pop()
        require(clazz.isInstance(stage)) {
            "popped stage expected to be $clazz, but got ${stage.javaClass}"
        }
    }

    internal fun pop(): TickStage {
        checkOnThread()

        val stage = activeStages.removeLast().also(history::add)
        stacktraces.removeLastOrNull()
        Reden.LOGGER.debug("TickStageTree: [{}] pop {}", activeStages.size, stage)
        stage.status = TickStage.StageStatus.Ticked
        stage.postTick()
        stage.status = TickStage.StageStatus.Finished
        if (stage == stepOverUntil) {
            Reden.LOGGER.debug("stage == stepOverUntil")
            stepOverUntil = null
            stepOverCallback?.invoke()
            stepOverCallback = null
            server.data.freeze("step-over")
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
            pop(stage.javaClass)
        }
    }

    fun stepOver(activeStage: TickStage, callback: () -> Unit): Boolean {
        stepOverUntil = activeStage
        stepOverCallback = callback
        steppingInto = false
        server.data.frozen = false
        return true
    }

    fun stepInto(callback: () -> Unit) {
        synchronized(this) {
            // Here we use synchronized to make it able to be called from other threads
            // @see com.github.zly2006.reden.network.Pause
            stepOverUntil = null
            steppingInto = true
            stepIntoCallback = callback
            server.data.frozen = false
        }
    }

    fun onBlockChanging(pos: BlockPos, state: BlockState, world: ServerWorld) {
        if ((activeStage as? TickStageWorldProvider)?.world == null) {
            // Note: no available world, we should add a stage to track this block change
            // This is usually caused by other mods.
            push(TickStageWorldProvider("set_block", activeStage!!, world))
        }
        val stage = activeStage as? TickStageWithWorld ?: return
        val oldState = stage.world?.getBlockState(pos) ?: return
        activeStage!!.changedBlocks.computeIfAbsent(pos) { TickStage.BlockChange(oldState, state) }
    }

    fun onBlockChanged(pos: BlockPos, state: BlockState) {
        val stage = activeStage
        if (stage is TickStageWorldProvider && stage.name == "set_name") {
            pop(TickStageWorldProvider::class.java)
        }
    }

    fun <T> onTickScheduled(orderedTick: OrderedTick<T>) {
        (orderedTick as TickStageTreeOwnerAccess).tickStageTree = this
        activeStage?.hasScheduledTicks = true
    }

    fun onBlockEventAdded(blockEvent: BlockEvent) {
        (blockEvent as TickStageTreeOwnerAccess).tickStageTree = this
        activeStage?.hasBlockEvents = true
    }
}
