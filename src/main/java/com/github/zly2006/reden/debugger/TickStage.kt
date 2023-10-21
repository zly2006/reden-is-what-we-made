package com.github.zly2006.reden.debugger

import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val parent: TickStage?,
) {
    init {
        val nameRegex = Regex("[\\w\\-]+")
        require(nameRegex.matches(name)) { "Invalid tick stage name: $name" }
    }
    val displayName = Text.translatable("reden.debugger.tick_stage.$name")
    val description = Text.translatable("reden.debugger.tick_stage.$name.desc")
    val children = mutableListOf<TickStage>()

    open fun toByteBuf(buf: PacketByteBuf, parent: TickStage?) {
        buf.writeBoolean(parent != null)
        buf.writeString(name)
        buf.writeVarInt(children.size)
    }

    abstract fun tick()

    open fun reset(): Unit = throw UnsupportedOperationException("Reset not supported for tick stage $name")
}
