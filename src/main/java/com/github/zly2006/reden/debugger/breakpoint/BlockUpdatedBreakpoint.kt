package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import io.wispforest.owo.ui.container.FlowLayout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.text.Text

@Serializable
class BlockUpdatedBreakpoint(
    @SerialName("idaaa")
    override var id: Int
) : BlockUpdateEvent(id) {
    override val type get() = Companion
    companion object: BreakPointType {
        override val id = Reden.identifier("block_updated")
        override val description: Text = Text.literal("BlockUpdated")
        override fun create(id: Int) = BlockUpdatedBreakpoint(id)

        override fun appendCustomFieldsUI(parent: FlowLayout, breakpoint: BreakPoint) {
            BlockUpdateEvent.appendCustomFieldsUI(parent, breakpoint)
        }

        override fun kSerializer(): KSerializer<out BreakPoint> {
            return BlockUpdateEvent.serializer()
        }
    }

    override fun call(event: Any) {
        if ((event as AbstractBlockUpdateStage<*>).targetPos == pos) {
            super.call(event)
        }
    }
}
