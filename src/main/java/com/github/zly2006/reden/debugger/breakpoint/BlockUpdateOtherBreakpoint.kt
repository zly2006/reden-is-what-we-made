package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import io.wispforest.owo.ui.container.FlowLayout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.text.Text

@Serializable
class BlockUpdateOtherBreakpoint(
    @SerialName("_unused_id")
    override var id: Int
) : BlockUpdateEvent(id) {
    override val type get() = Companion
    companion object: BreakPointType {
        override val id = Reden.identifier("block_update_other")
        override val description = Text.literal("BlockUpdateOther")
        override fun create(id: Int) = BlockUpdateOtherBreakpoint(id)
        override fun appendCustomFieldsUI(parent: FlowLayout, breakpoint: BreakPoint) {
            BlockUpdateEvent.appendCustomFieldsUI(parent, breakpoint)
        }
        override fun kSerializer(): KSerializer<out BreakPoint> {
            return BlockUpdateEvent.serializer()
        }
    }

    override fun call(event: Any) {
        if ((event as AbstractBlockUpdateStage<*>).sourcePos == pos) {
            super.call(event)
        }
    }
}
