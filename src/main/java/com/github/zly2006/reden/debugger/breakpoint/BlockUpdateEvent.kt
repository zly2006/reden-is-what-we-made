package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.debugger.stages.block.NeighborChanged
import com.github.zly2006.reden.debugger.stages.block.StageBlockPPUpdate
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

@Serializable
sealed class BlockUpdateEvent(
    override var id: Int,
    var options: Int = 0,
    @Serializable(with = BlockPosSerializer::class)
    override var pos: BlockPos? = null,
): BreakPoint {
    abstract override val type: BreakPointType
    companion object {
        fun appendCustomFieldsUI(parent: FlowLayout, breakpoint: BreakPoint) {
            breakpoint as BlockUpdateEvent
            val mc = MinecraftClient.getInstance()
            parent.child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(
                    Components.checkbox(Text.literal("NeighborChanged")).apply {
                        checked(breakpoint.options and NC > 0)
                        onChanged {
                            breakpoint.options = if (it) {
                                breakpoint.options or NC
                            } else {
                                breakpoint.options and NC.inv()
                            }
                            mc.data.breakpoints.sync(breakpoint)
                        }
                    }
                )
                child(
                    Components.checkbox(Text.literal("PostPlacement")).apply {
                        checked(breakpoint.options and PP > 0)
                        onChanged {
                            breakpoint.options = if (it) {
                                breakpoint.options or PP
                            } else {
                                breakpoint.options and PP.inv()
                            }
                            mc.data.breakpoints.sync(breakpoint)
                        }
                    }
                )
                child(
                    Components.checkbox(Text.literal("ComparatorUpdate")).apply {
                        checked(breakpoint.options and CU > 0)
                        onChanged {
                            breakpoint.options = if (it) {
                                breakpoint.options or CU
                            } else {
                                breakpoint.options and CU.inv()
                            }
                            mc.data.breakpoints.sync(breakpoint)
                        }
                    }
                )
            })
        }

        const val PP = 1
        const val NC = 2
        // todo
        const val CU = 4
    }
    object BlockUpdatedBreakpoint: BreakPointType {
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
    override var name: String = ""
    override var flags: Int = ENABLED
    @Serializable(with = IdentifierSerializer::class)
    override var world: Identifier? = null
    override val handler: MutableList<BreakPoint.Handler> = mutableListOf()
    override fun call(event: Any) {
        if (event !is AbstractBlockUpdateStage<*>) {
            throw RuntimeException("BlockUpdateEvent can only be called by AbstractBlockUpdateStage")
        }
        if (options and PP > 0 && event is StageBlockPPUpdate) {
            super.call(event)
        }
        if (options and NC > 0 && event is NeighborChanged) {
            super.call(event)
        }
    }

    override fun setPosition(pos: BlockPos) {
        this.pos = pos
    }

    override fun toString() = buildString {
        append("BlockUpdateEvent(")
        if (options and PP > 0) {
            append("PP")
        }
        if (options and NC > 0) {
            append("NC")
        }
        if (options and CU > 0) {
            append("CU")
        }
        append(')')
        append(pos?.toShortString())
    }
}
