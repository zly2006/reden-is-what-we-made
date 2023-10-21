package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.debugger.TickStage

/**
 * TODO
 */
class StageTree: Iterator<TickStage> {
    class TreeNode(
        val parent: TreeNode?,
        val stage: TickStage,
        var iter: Iterator<TickStage>?,
    )
    var root: TreeNode? = null
    var child: TreeNode? = null
    val tickedStages = mutableListOf<TickStage>()
    override fun hasNext(): Boolean {
        if (child == null)
            return false
        var stage = child!!
        if (stage.iter == null) {
            return true
        }
        while (!stage.iter!!.hasNext()) {
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
        tickedStages.add(child!!.stage)

        if (child!!.iter == null) {
            child!!.iter = child!!.stage.children.iterator()
            return child!!.stage
        } else {
            while (!child!!.iter!!.hasNext()) {
                if (child == root) {
                    error("No child")
                }
                child = child!!.parent!!
            }

            val next = child!!.iter!!.next()
            child = TreeNode(child, next, null)
            return next
        }
    }

    fun clear() {
        tickedStages.clear()
        root = null
        child = null
    }

    fun resetTo(stage: TickStage) {

    }
}
