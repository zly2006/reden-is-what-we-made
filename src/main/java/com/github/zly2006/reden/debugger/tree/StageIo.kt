package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.DummyStage
import com.github.zly2006.reden.debugger.stages.block.StageBlockNCUpdate
import com.github.zly2006.reden.debugger.stages.block.StageBlockNCUpdateSixWay
import com.github.zly2006.reden.debugger.stages.block.StageBlockNCUpdateWithSource
import com.github.zly2006.reden.debugger.stages.block.StageBlockPPUpdate
import com.github.zly2006.reden.debugger.tree.StageIo.Constructor
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld

object StageIo {
    fun interface Constructor {
        fun construct(parent: TickStage?): TickStage
    }
    val constructors = mutableMapOf<String, Constructor>()

    init {
        class EmptyTickStage(name: String, parent: TickStage?) : TickStage(name, parent) {
            override fun tick() {
            }
        }
        class EmptyWorldTickStage(name: String, parent: TickStage?) : TickStage(name, parent), TickStageWithWorld {
            override lateinit var world: ServerWorld
            override fun tick() {
            }
        }

        // Note: these stages have no extra data, so we can use empty constructor to simplify code
        constructors["server_root"] = Constructor { EmptyTickStage("server_root", it) }
        constructors["world_root"] = Constructor { EmptyTickStage("world_root", it) }
        constructors["end"] = Constructor { EmptyTickStage("end", it) }
        constructors["network"] = Constructor { EmptyTickStage("network", it) }

        constructors["nc_update"] = Constructor { StageBlockNCUpdate(it!!, null) }
        constructors["nc_update_6"] = Constructor { StageBlockNCUpdateSixWay(it!!, null) }
        constructors["nc_update_with_source"] = Constructor { StageBlockNCUpdateWithSource(it!!, null) }
        constructors["pp_update"] = Constructor { StageBlockPPUpdate(it!!, null) }
        constructors["update_block"] = Constructor { EmptyWorldTickStage("update_block", it!!) }
    }

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

    fun readStageTree(buf: PacketByteBuf): StageTree {
        val tree = StageTree()
        val size = buf.readVarInt()
        var prevNode: StageTree.TreeNode? = null
        for (i in 0 until size) {
            val childrenUpdated = buf.readBoolean()
            val iterIndex = buf.readVarInt()
            val stage = readStage(prevNode?.stage, buf)
            val node = StageTree.TreeNode(
                prevNode,
                stage,
                childrenUpdated,
                if (childrenUpdated) stage.children.listIterator(iterIndex) else null
            )
            if (prevNode == null) {
                tree.root = node
            } else {
                if (iterIndex != 0) {
                    // set children if possible
                    // todo: here is a bug
                    //  prevNode.stage.children[iterIndex - 1] = node.stage
                }
            }
            prevNode = node
        }
        tree.child = prevNode
        return tree
    }
}