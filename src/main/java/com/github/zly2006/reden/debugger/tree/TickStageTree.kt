package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.debugger.TickStage

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
        activeStages.add(stage)
        stage.preTick()
    }

    internal fun pop(): TickStage {
        /*
        if (stage4checking != null) {
            require(stage4checking == activeStage) {
                "Stage $stage4checking is not the active stage"
            }
        }

         */
        val stage = activeStages.removeLast().also(history::add)
        stage.postTick()
        if (steppingInto) {
            steppingInto = false
            stepIntoCallback?.invoke()
            stepIntoCallback = null
        }
        else if (stage == stepOverUntil) {
            stepOverUntil = null
            stepOverCallback?.invoke()
            stepOverCallback = null
        }
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
        return true
    }

    fun stepInto(callback: () -> Unit) {
        stepOverUntil = null
        steppingInto = true
        stepIntoCallback = callback
    }
}
