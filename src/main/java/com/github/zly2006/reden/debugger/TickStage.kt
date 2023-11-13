package com.github.zly2006.reden.debugger

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
}
