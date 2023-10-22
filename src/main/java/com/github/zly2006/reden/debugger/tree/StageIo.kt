package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.DummyStage
import net.minecraft.network.PacketByteBuf

object StageIo {
    interface Constructor {
        fun construct(parent: TickStage?): TickStage
    }
    val constructors = mutableMapOf<String, Constructor>()

    fun writeStage(stage: TickStage, buf: PacketByteBuf) {
        buf.writeBoolean(stage.parent != null)
        buf.writeVarInt(stage.children.size)
        buf.writeString(stage.name)

        stage.writeByteBuf(buf)
    }

    fun readStage(parent: TickStage?, buf: PacketByteBuf): TickStage {
        val hasParent = buf.readBoolean()
        if (hasParent && parent == null) {
            error("Stage has parent but parent is null")
        }
        val childrenSize = buf.readVarInt()
        val name = buf.readString()

        val stage = constructors[name]?.construct(parent) ?: error("Unknown stage name: $name")
        stage.readByteBuf(buf)

        for (i in 0 until childrenSize) {
            stage.children.add(DummyStage(stage))
        }

        return stage
    }

    fun writeStageTree(tree: StageTree, buf: PacketByteBuf) {
        val list = mutableListOf<StageTree.TreeNode>()
        var node = tree.child
        while (node != null) {
            list.add(node)
            node = node.parent
        }

        buf.writeVarInt(list.size)
        for (i in list.size - 1 downTo 0) {
            node = list[i]
            buf.writeBoolean(node.childrenUpdated)
            if (node.childrenUpdated) {
                if (node.iter == null) {
                    node.iter = node.stage.children.listIterator()
                }
                buf.writeVarInt(node.iter!!.nextIndex())
            }
            writeStage(node.stage, buf)
        }
    }

    fun readStageTree(parent: TickStage?, buf: PacketByteBuf): StageTree {
        val tree = StageTree()
        val size = buf.readVarInt()
        var prevNode: StageTree.TreeNode? = null
        for (i in 0 until size) {
            val childrenUpdated = buf.readBoolean()
            val iterIndex = if (childrenUpdated) buf.readVarInt() else 0
            val stage = readStage(parent, buf)
            val node = StageTree.TreeNode(
                prevNode,
                stage,
                childrenUpdated,
                if (childrenUpdated) stage.children.listIterator(iterIndex) else null
            )
            if (prevNode == null) {
                tree.root = node
            } else {
                prevNode.stage.children[iterIndex - 1] = node.stage
            }
            prevNode = node
        }
        tree.child = prevNode
        return tree
    }
}