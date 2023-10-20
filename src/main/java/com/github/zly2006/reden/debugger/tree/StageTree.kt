package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.debugger.TickStage

/**
 * TODO
 */
class StageTree: Iterator<TickStage> {
    var root: TickStage? = null
    var child: TickStage? = null
    val tickedStages = mutableListOf<TickStage>()
    override fun hasNext(): Boolean {
        if (child == null)
            return false
        var stage = child!!
        while (!stage.hasNext()) {
            if (stage == root) {
                return false
            }
            stage = stage.parent!!
        }
        return true
    }

    override fun next(): TickStage {
        if (child == null) {
            error("No child")
        }
        tickedStages.add(child!!)

        while (!child!!.hasNext()) {
            if (child == root) {
                error("No child")
            }
            child = child!!.parent!!
        }

        return child!!
    }

    fun clear() {
        tickedStages.clear()
        root = null
        child = null
    }

    fun resetTo(stage: TickStage) {
        do {
            child!!.reset()
            child = tickedStages.removeLast()
        } while (child != stage)
    }
}
