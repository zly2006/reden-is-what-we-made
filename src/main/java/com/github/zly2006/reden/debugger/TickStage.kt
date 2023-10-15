package com.github.zly2006.reden.debugger

import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val parent: TickStage? = null,
): Iterator<TickStage> {
    val displayName = Text.translatable("reden.debugger.tick_stage.$name")
    val description = Text.translatable("reden.debugger.tick_stage.$name.desc")
    val children = mutableListOf<TickStage>()
    private var childrenIterator = children.listIterator()

    override fun hasNext() = childrenIterator.hasNext()

    override fun next() = childrenIterator.next()

    open fun tick() {
        childrenIterator = children.listIterator()
    }

    open fun reset(): Unit = throw UnsupportedOperationException("Reset not supported for tick stage $name")
}
