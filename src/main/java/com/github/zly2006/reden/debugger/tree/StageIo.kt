package com.github.zly2006.reden.debugger.tree

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.DummyStage
import com.github.zly2006.reden.debugger.stages.block.*
import com.github.zly2006.reden.debugger.stages.world.*
import com.github.zly2006.reden.debugger.tree.StageIo.Constructor
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld

object StageIo {
    fun interface Constructor {
        fun construct(parent: TickStage?): TickStage
    }
    val constructors = mutableMapOf<String, Constructor>()

    init {
        class EmptyTickStage(name: String, parent: TickStage?): TickStage(name, parent), TickStageWithWorld {
            override val world: ServerWorld?
                get() {
                    Reden.LOGGER.warn("Accessing world from client side at $name")
                    return null
                }
        }

        // Note: these stages have no extra data, so we can use empty constructor to simplify code
        constructors["server_root"] = Constructor { EmptyTickStage("server_root", it) }
        constructors["end"] = Constructor { EmptyTickStage("end", it) }
        constructors["global_network"] = Constructor { EmptyTickStage("global_network", it) }

        constructors["world_root"] = Constructor { EmptyTickStage("world_root", it) }
        constructors["network"] = Constructor { EmptyTickStage("network", it) }
        constructors["update_block"] = Constructor { EmptyTickStage("update_block", it!!) }
        constructors["commands_stage"] = Constructor { EmptyTickStage("commands_stage", it!!) }

        constructors["block_update"] = Constructor { BlockUpdateStage(it!!) }
        constructors["nc_update"] = Constructor { StageBlockNCUpdate(it!!, null) }
        constructors["nc_update_1"] = Constructor { StageBlockNCUpdateSixWay.StageBlockNCUpdateOneWay(it!!, direction = null) }
        constructors["nc_update_6"] = Constructor { StageBlockNCUpdateSixWay(it!!, null) }
        constructors["nc_update_with_source"] = Constructor { StageBlockNCUpdateWithSource(it!!, null) }
        constructors["pp_update"] = Constructor { StageBlockPPUpdate(it!!, null) }

        constructors["entities"] = Constructor { EntitiesRootStage(null) }
        constructors["entity"] = Constructor { EntityStage(it as EntitiesRootStage, null) }
        constructors["block_entities_root"] = Constructor { BlockEntitiesRootStage(null) }
        constructors["block_entity"] = Constructor { BlockEntityStage(it as BlockEntitiesRootStage, null) }
        constructors["block_events_root"] = Constructor { BlockEventsRootStage(null) }
        constructors["block_event"] = Constructor { BlockEventStage(it as BlockEventsRootStage, null) }
        constructors["block_scheduled_ticks_root"] = Constructor { BlockScheduledTicksRootStage(null) }
        constructors["block_scheduled_tick"] = Constructor { BlockScheduledTickStage(it as BlockScheduledTicksRootStage, null) }
        constructors["fluid_scheduled_ticks_root"] = Constructor { FluidScheduledTicksRootStage(null) }
        constructors["fluid_scheduled_tick"] = Constructor { FluidScheduledTickStage(it as FluidScheduledTicksRootStage, null) }

        constructors["raid"] = Constructor { EmptyTickStage("raid", it!!) }
        constructors["random_tick"] = Constructor { EmptyTickStage("random_tick", it!!) }
        constructors["spawn"] = Constructor { EmptyTickStage("spawn", it!!) }
        constructors["special_spawn"] = Constructor { EmptyTickStage("special_spawn", it!!) }
        constructors["time"] = Constructor { EmptyTickStage("time", it!!) }
        constructors["weather"] = Constructor { EmptyTickStage("weather", it!!) }
        constructors["world_border"] = Constructor { EmptyTickStage("world_border", it!!) }
    }

    @Deprecated("", replaceWith = ReplaceWith("writeSingleTickStage", imports = ["com.github.zly2006.reden.debugger.tree.StageIo"]))
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

        val stage = constructors[name]?.construct(parent)
            ?: error("Unknown stage name: $name")
        stage.readByteBuf(buf)

        for (i in 0 until childrenSize) {
            stage.children.add(DummyStage(stage))
        }

        return stage
    }

    fun writeStageTree(buf: PacketByteBuf, tree: StageTree) {
        val list = tree.currentNodes

        buf.writeVarInt(list.size)
        for (i in list.size - 1 downTo 0) {
            val node = list[i]
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
            val iterIndex = if (childrenUpdated) buf.readVarInt() else 0
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
                   // prevNode.stage.children[iterIndex - 1] = node.stage
                }
            }
            prevNode = node
        }
        tree.child = prevNode
        tree.lastReturned = prevNode
        return tree
    }

    /**
     * Def: node=
     *
     * {
     *
     * - name
     * - custom data
     * - if children updated:
     *
     * }
     *
     * - (in) entries
     * - (foreach) i
     * - children [i] node
     */
    fun writeStageTreeNode(buf: PacketByteBuf, node: StageTree.TreeNode, writeAllChildren: Boolean) {
        buf.writeString(node.stage.name)
        node.stage.writeByteBuf(buf)
        buf.writeBoolean(node.childrenUpdated)
        if (node.childrenUpdated) {
            if (node.iter == null) {
                node.iter = node.stage.children.listIterator()
            }
            buf.writeVarInt(node.stage.children.size)
            buf.writeVarInt(node.iter!!.nextIndex())
        }
    }

    fun writeTickStageTree(packetByteBuf: PacketByteBuf, tickStageTree: TickStageTree) {
        writeTickStageTree(packetByteBuf, tickStageTree, false)
    }

    fun writeTickStageTree(packetByteBuf: PacketByteBuf, tickStageTree: TickStageTree, writeAllChildren: Boolean) {
        packetByteBuf.writeBoolean(writeAllChildren)
        if (writeAllChildren) {
            TODO()
        }

        val list = tickStageTree.activeStages
        packetByteBuf.writeCollection(list, ::writeSingleTickStage)
    }

    fun writeSingleTickStage(buf: PacketByteBuf, tickStage: TickStage) {
        buf.writeNullable(tickStage.parent?.id, PacketByteBuf::writeVarInt)
        buf.writeVarInt(tickStage.id)
        buf.writeVarInt(tickStage.children.size)
        buf.writeString(tickStage.name)
        tickStage.writeByteBuf(buf)
    }

    fun readTickStageTree(packetByteBuf: PacketByteBuf): TickStageTree {
        val writeAllChildren = packetByteBuf.readBoolean()
        if (writeAllChildren) {
            TODO()
        }
        var lastRead: TickStage? = null
        fun readSingleTickStage(buf: PacketByteBuf): TickStage {
            val parentId = buf.readNullable(PacketByteBuf::readVarInt)
            val id = buf.readVarInt()
            val childrenSize = buf.readVarInt()
            val name = buf.readString()

            val stage = constructors[name]?.construct(lastRead)
                ?: error("Unknown stage name: $name")
            stage.readByteBuf(buf)

            for (i in 0 until childrenSize) {
                stage.children.add(DummyStage(stage))
            }

            lastRead = stage
            return stage
        }

        val list = packetByteBuf.readCollection(::ArrayList, ::readSingleTickStage)
        return TickStageTree(activeStages = list)
    }
}
