package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.debugger.TickStage

/**
 * TODO
 */
class StageTree: Iterator<TickStage> {
    class TreeNode(
        val parent: TreeNode?,
        val stage: TickStage,
        var childrenUpdated: Boolean,
        var iter: ListIterator<TickStage>?,
    )
    var root: TreeNode? = null
    var child: TreeNode? = null
    val tickedStages = mutableListOf<TickStage>()
    override fun hasNext(): Boolean {
        if (child == null)
            return false
        if (!child!!.childrenUpdated)
            return true
        checkIterators()
        return child != null
    }

    /**
     * Assume that the child node has been ticked and non-null.
     */
    private fun checkIterators() {
        if (child!!.iter == null) {
            child!!.iter = child!!.stage.children.listIterator()
        }

        while (child?.iter?.hasNext() == false) {
            child = child!!.parent
        }
    }

    override fun next(): TickStage {
        if (child == null) {
            error("No child")
        }

        // if we have not ticked the child node, tick it
        if (!child!!.childrenUpdated) {
            child!!.childrenUpdated = true
        } else {
            checkIterators()
            val next = child!!.iter!!.next()
            child = TreeNode(
                child,
                next,
                childrenUpdated = true, // we returned this stage, it should be ticked.
                null
            )
        }

        tickedStages.add(child!!.stage)
        return child!!.stage
    }

    fun clear() {
        tickedStages.clear()
        root = null
        child = null
    }

    fun resetTo(stage: TickStage) {
        TODO("still need to reset all iterators to current tick stage")
    }
}
