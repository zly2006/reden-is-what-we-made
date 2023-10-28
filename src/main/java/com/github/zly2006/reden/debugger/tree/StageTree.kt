package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.disableWatchDog
import com.github.zly2006.reden.utils.server
import okhttp3.internal.toHexString
import org.jetbrains.annotations.TestOnly

class StageTree: Iterator<TickStage> {
    class TreeNode(
        val parent: TreeNode?,
        val stage: TickStage,
        var childrenUpdated: Boolean,
        var iter: ListIterator<TickStage>?,
    ) {
        override fun toString() = "$stage ${if (!childrenUpdated) "<>" else "${iter?.previousIndex()} / ${stage.children.size}"}"
    }
    var root: TreeNode? = null
    var child: TreeNode? = null
    val tickedStages = mutableListOf<TickStage>()
    private var lastReturned: TreeNode? = null
    /*
    root
    1
    2
     \
     3
    / \
   4   7
  /  \
 5    6
   -- all children ticked

     */
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
        lastReturned = child
        Reden.LOGGER.trace("[StageTree#next] {}", child)
        return lastReturned!!.stage
    }

    fun clear() {
        tickedStages.clear()
        root = null
        lastReturned = null
        child = null
    }

    fun resetTo(stage: TickStage) {
        TODO("still need to reset all iterators to current tick stage")
    }

    fun pauseGame() {
        server.timeReference = Long.MAX_VALUE
        disableWatchDog = true
    }

    fun peekLeaf(): TickStage {
        return lastReturned?.stage
            ?: error("No last returned")
    }

    fun initRoot(serverRootStage: TickStage, childrenUpdated: Boolean) {
        clear()
        root = TreeNode(null, serverRootStage, childrenUpdated, null)
        child = root
        lastReturned = root
    }

    @TestOnly
    fun assertInTree(stage: TickStage) {
        var node = child
        while (node != null) {
            if (node.stage == stage) {
                return
            }
            node = node.parent
        }
        error("Stage $stage not in this tree.")
    }

    @TestOnly
    fun printTree() {
        val list = mutableListOf<TreeNode>()
        var node = lastReturned
        while (node != null) {
            list.add(node)
            node = node.parent
        }
        list.reverse()
        println("===== Tick Stage Tree =====")
        list.forEach {
            println("${it.hashCode().toHexString()} $it")
        }
        println("===== End Tree =====")
    }

    fun insert2child(stage: TickStage) {
        val child = lastReturned ?: error("No child, check peekLeaf().")
        if (!child.childrenUpdated) {
            error("Child not ticked, tick it first!")
        }
        insert2child(child.stage, stage)
    }

    fun insert2child(parent: TickStage, stage: TickStage) {
        Reden.LOGGER.trace("[StageTree#insert2child] into {} -> {}", parent, stage)

        var node = lastReturned
        while (node != null) {
            if (node.stage == parent) {
                break
            }
            node = node.parent
        }
        if (node == null) {
            error("Parent $parent not found in this tree.")
        }

        if (node.iter == null) {
            node.iter = node.stage.children.listIterator()
        }
        val nodeIter = node.iter as? MutableListIterator<TickStage>
            ?: error("Child iter is not mutable")
        nodeIter.add(stage)
        nodeIter.previous() // move back to the inserted stage
        if (child == null) {
            // Oops, the tree is empty, we need to update the child node.
            child = node
        }
    }
}
