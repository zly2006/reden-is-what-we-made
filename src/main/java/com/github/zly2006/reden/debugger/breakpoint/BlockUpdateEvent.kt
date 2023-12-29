package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.debugger.stages.block.NeighborChanged
import com.github.zly2006.reden.debugger.stages.block.StageBlockComparatorUpdate
import com.github.zly2006.reden.debugger.stages.block.StageBlockPPUpdate
import com.github.zly2006.reden.network.UpdateBreakpointPacket.Companion.ENABLED
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Sizing
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

@Serializable
sealed class BlockUpdateEvent: BreakPoint {
    override var id: Int = 0
    @Serializable(with = BlockPosSerializer::class)
    override var pos: BlockPos? = null
    companion object {
        fun appendCustomFieldsUI(parent: FlowLayout, breakpoint: BreakPoint) {
            breakpoint as BlockUpdateEvent
            val mc = MinecraftClient.getInstance()
            parent.child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
                child(
                    Components.checkbox(Text.literal("NeighborChanged")).apply {
                        checked(breakpoint.flags and NC > 0)
                        onChanged {
                            breakpoint.flags = if (it) {
                                breakpoint.flags or NC
                            } else {
                                breakpoint.flags and NC.inv()
                            }
                            mc.data.breakpoints.syncFlags(breakpoint)
                        }
                    }
                )
                child(
                    Components.checkbox(Text.literal("PostPlacement")).apply {
                        checked(breakpoint.flags and PP > 0)
                        onChanged {
                            breakpoint.flags = if (it) {
                                breakpoint.flags or PP
                            } else {
                                breakpoint.flags and PP.inv()
                            }
                            mc.data.breakpoints.syncFlags(breakpoint)
                        }
                    }
                )
                child(
                    Components.checkbox(Text.literal("ComparatorUpdate")).apply {
                        checked(breakpoint.flags and CU > 0)
                        onChanged {
                            breakpoint.flags = if (it) {
                                breakpoint.flags or CU
                            } else {
                                breakpoint.flags and CU.inv()
                            }
                            mc.data.breakpoints.syncFlags(breakpoint)
                        }
                    }
                )
            })
        }

        const val PP = 1 shl 16
        const val NC = 1 shl 17
        const val CU = 1 shl 18

        /**
         * @see com.github.zly2006.reden.debugger.TickStage.StageStatus.Pending
         */
        const val PRE = 1 shl 30
        /**
         * @see com.github.zly2006.reden.debugger.TickStage.StageStatus.Ticked
         */
        const val POST = 1 shl 31
    }

    override var name: String = ""
    override var flags: Int = ENABLED or NC or PRE
    @Serializable(with = IdentifierSerializer::class)
    override var world: Identifier? = null
    override val handler: MutableList<BreakPoint.Handler> = mutableListOf()
    override fun call(event: Any) {
        if (event !is AbstractBlockUpdateStage<*>) {
            throw RuntimeException("BlockUpdateEvent can only be called by AbstractBlockUpdateStage")
        }
        if (!when (event.status) {
                TickStage.StageStatus.Pending -> flags and PRE != 0
                TickStage.StageStatus.Ticked -> flags and POST != 0
                else -> false
            }
        ) return
        if (!when (event) {
                is StageBlockPPUpdate -> flags and PP != 0
                is NeighborChanged -> flags and NC != 0
                is StageBlockComparatorUpdate -> flags and CU != 0
                else -> false
            }
        ) return
        super.call(event)
    }

    override fun setPosition(pos: BlockPos) {
        this.pos = pos
    }

    override fun toString() = buildString {
        append("BlockUpdateEvent(")
        if (flags and PP > 0) {
            append("PP")
        }
        if (flags and NC > 0) {
            append("NC")
        }
        if (flags and CU > 0) {
            append("CU")
        }
        append(')')
        append(pos?.toShortString())
    }
}
