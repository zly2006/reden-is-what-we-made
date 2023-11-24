package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.disableWatchDog
import com.github.zly2006.reden.utils.debugLogger
import com.github.zly2006.reden.utils.server
import io.netty.util.internal.UnstableApi
import okhttp3.internal.toHexString
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.TestOnly

/**
 * A StageTree represents something like the JVM method calling stack.
 * Each [TickStage] is one or some method calls, representing how the server is ticking.
 *
 * A StageTree is an iterator of [TickStage]s, it will return the next stage to tick
 * and ticking the stage is to tick the server like vanilla.
 *
 * A StageTree is mutable, you can insert a stage into it (at the cursor).
 *
 * You cannot remove a stage from a StageTree, because it is not necessary.
 */
class StageTree: Iterator<TickStage> {
    /**
     * A node in the tree.
     *
     * if [StageTree.next] is called, firstly, we check if current [stage] was ticked
     * by checking [childrenUpdated].
     * Because we assume that each tick will clear its children and update them when ticked.
     *
     * If [childrenUpdated] is false, we will tick the stage and set [childrenUpdated] to true.
     *
     * If [childrenUpdated] is true, we will tick all its children.
     */
    class TreeNode(
        val parent: TreeNode?,
        val stage: TickStage,
        var childrenUpdated: Boolean,
        var iter: ListIterator<TickStage>?,
    ) {
        override fun toString() = "$stage ${if (!childrenUpdated) "<>" else "${iter?.previousIndex()} / ${stage.children.size}"}"
    }

    companion object {
        val debug = System.getProperty("reden.debugger.log", "false").toBoolean()
    }

    var root: TreeNode? = null
    var child: TreeNode? = null
    val tickedStages = mutableListOf<TickStage>()
    internal var lastReturned: TreeNode? = null

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
     *
     * (i.e. [child] != null && [TreeNode.childrenUpdated])
     */
    private fun checkIterators() {
        if (child!!.iter == null) {
            child!!.iter = child!!.stage.children.listIterator()
        }

        while (child?.iter?.hasNext() == false) {
            child!!.stage.endTask()
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
        if (debug) debugLogger("[StageTree#next] $child")
        return lastReturned!!.stage
    }

    fun clear() {
        tickedStages.clear()
        root = null
        lastReturned = null
        child = null
    }

    @UnstableApi
    fun resetTo(stage: TickStage) {
        val index = tickedStages.indexOf(stage)
        val stagesToReset = (tickedStages.size - 1 downTo index + 1).map {
            tickedStages.removeAt(it)
        }
        stagesToReset.forEach { it.reset() }
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
        if (debug) debugLogger("StageTree.insert2child $stage -> $parent")
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

    /**
     * WARNING: DO NOT USE THIS METHOD IF YOU DONT KNOW WHAT IT IS
     */
    @ApiStatus.Internal
    fun resetIterator(stage: TickStage) {
        if (debug) debugLogger("StageTree.resetIterator -> $stage")
        var node = lastReturned
        while (node != null) {
            if (node.stage == stage) {
                break
            }
            node = node.parent
        }

        if (node == null) return
        if (node.iter == null) return
        node.iter = stage.children.listIterator(node.iter!!.nextIndex())
    }
}
