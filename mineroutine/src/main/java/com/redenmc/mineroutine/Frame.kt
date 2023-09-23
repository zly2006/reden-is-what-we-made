package com.redenmc.mineroutine

class Frame(
    val stack: RunStack,
    val name: String,
    val parent: Frame?,
    val tickStage: TickStage
) {
    fun tick() {

    }
}