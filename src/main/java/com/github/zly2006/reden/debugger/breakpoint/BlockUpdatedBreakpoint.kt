package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import net.minecraft.text.Text

class BlockUpdatedBreakpoint(id: Int) : BlockUpdateEvent(id, Companion) {
    companion object: BreakPointType {
        override val id = Reden.identifier("block_updated")
        override val description: Text = Text.literal("BlockUpdated")
        override fun create(id: Int) = BlockUpdatedBreakpoint(id)
    }
}
