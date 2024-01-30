package com.github.zly2006.reden.debugger.events

import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

@Deprecated("Not implemented")
class BlockChangedEvent(
    val pos: BlockPos,
    val oldState: BlockState,
    val newState: BlockState
) {
    fun fire() {
        // todo
    }
}
