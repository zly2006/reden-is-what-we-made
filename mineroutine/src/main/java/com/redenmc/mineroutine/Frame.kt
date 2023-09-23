package com.redenmc.mineroutine

open class Frame(
    val stack: RunStack,
    val name: String,
    val parent: Frame?,
    val tickStage: TickStage
) {
    open fun tick() {
    }
}
