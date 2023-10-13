package com.github.zly2006.reden.debugger

import net.minecraft.text.Text

abstract class TickStage(
    val name: String,
    val nextStage: TickStage? = null,
): Iterator<TickStage> {
    val displayName = Text.translatable("reden.debugger.tick_stage.$name")
    val description = Text.translatable("reden.debugger.tick_stage.$name.desc")
    val children = mutableListOf<TickStage>()
    val childrenIterator = children.listIterator()

    override fun hasNext(): Boolean {
        return childrenIterator.hasNext() || nextStage != null
    }

    override fun next(): TickStage {
        return if (childrenIterator.hasNext()) {
            childrenIterator.next()
        } else {
            nextStage ?: throw NoSuchElementException("No more tick stages")
        }
    }

    abstract fun tick()

    open fun reset(): Unit = throw UnsupportedOperationException("Reset not supported for tick stage $name")
}
