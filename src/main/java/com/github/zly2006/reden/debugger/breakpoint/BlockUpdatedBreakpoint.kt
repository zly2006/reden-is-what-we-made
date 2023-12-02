package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import net.minecraft.text.Text

class BlockUpdatedBreakpoint(id: Int) : BlockUpdateEvent(id, Companion) {
    companion object: BreakPointType {
        override val id = Reden.identifier("block_updated")
        override val description: Text = Text.literal("BlockUpdated")
        override fun create(id: Int) = BlockUpdatedBreakpoint(id)
    }

    override fun call(event: Any) {
        if ((event as AbstractBlockUpdateStage<*>).targetPos == pos) {
            super.call(event)
        }
    }
}
