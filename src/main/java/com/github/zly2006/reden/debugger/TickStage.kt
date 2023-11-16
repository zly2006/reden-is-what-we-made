package com.github.zly2006.reden.debugger

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.stages.ServerRootStage
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val parent: TickStage?,
) {
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
    }

    open fun readByteBuf(buf: PacketByteBuf) {
    }

    /**
     * Run this tick stage.
     *
     *  Usually, this should call the caller of the target method,
     *       because in the caller there may have some mixins.
     */
    open fun tick() {
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
        while (tree.hasNext()) {
            val next = tree.next()
            next.tick()
            if (next == lastChildren) {
                break
            }
        }
    }
}
