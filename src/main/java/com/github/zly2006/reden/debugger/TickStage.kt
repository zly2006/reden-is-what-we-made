package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.ServerRootStage
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.utils.debugLogger
import com.github.zly2006.reden.utils.isDebug
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val parent: TickStage?,
) {
    private var debugExpectedChildrenSize = -1
    init {
        /*
        val nameRegex = Regex("[\\w\\-]+")
        require(nameRegex.matches(name)) { "Invalid tick stage name: $name" }
         */
    }
    open val displayName = Text.translatable("reden.debugger.tick_stage.$name")
    val description = Text.translatable("reden.debugger.tick_stage.$name.desc")
    val children = mutableListOf<TickStage>()

    open fun writeByteBuf(buf: PacketByteBuf) {
        if (isDebug) {
            buf.writeString(name)
        }
    }

    open fun readByteBuf(buf: PacketByteBuf) {
        if (isDebug) {
            val name = buf.readString()
            //println(name)
            assert(this.name == name) {
                "Tick stage name mismatch: $name != ${this.name}"
            }
        }
    }

    /**
     * Run this tick stage.
     *
     *  Usually, this should call the caller of the target method,
     *       because in the caller there may have some mixins.
     */
    open fun tick() {
        if (debugExpectedChildrenSize != -1 && debugExpectedChildrenSize != children.size) {
            error("Children should be null!!!!!")
        }
        debugExpectedChildrenSize = -1
        children.clear()
    }

    open fun reset(): Unit = throw UnsupportedOperationException("Reset not supported for tick stage $name")

    /**
     * Tick the server until last children is called.
     * Clawing this method can ensure the TAIL/RETURN injecting point is called after the vanilla logic executed.
     *
     * Note: assume that the root stage is [ServerRootStage].
     */
    fun yield() {
        var root = this
        while (root.parent != null) {
            root = root.parent!!
        }
        root as ServerRootStage
        val tree = root.server.data().tickStageTree
        val lastChildren = children.lastOrNull() ?: return
        if (StageTree.debug) debugLogger("StageTree.yield [=> $this")
        while (tree.hasNext()) {
            val next = tree.next()
            next.tick()
            if (next == lastChildren) {
                break
            }
        }

        debugExpectedChildrenSize = children.size
    }

    open fun endTask() {
    }
}
