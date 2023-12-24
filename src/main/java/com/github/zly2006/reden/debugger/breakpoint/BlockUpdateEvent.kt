package com.github.zly2006.reden.debugger.breakpoint

import com.github.zly2006.reden.access.ClientData.Companion.data
import com.github.zly2006.reden.debugger.stages.block.AbstractBlockUpdateStage
import com.github.zly2006.reden.debugger.stages.block.NeighborChanged
import com.github.zly2006.reden.debugger.stages.block.StageBlockPPUpdate
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.FlowLayout
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

abstract class BlockUpdateEvent(
    id: Int,
    type: BreakPointType,
    var options: Int = 0,
    override var pos: BlockPos? = null,
): BreakPoint(id, type) {
    companion object {
        fun appendCustomFieldsUI(parent: FlowLayout, breakpoint: BreakPoint) {
            breakpoint as BlockUpdateEvent
            val mc = MinecraftClient.getInstance()
            parent.child(
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
            parent.child(
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
            parent.child(
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
        }

        const val PP = 1
        const val NC = 2
        // todo
        const val CU = 4
    }
    override fun read(buf: PacketByteBuf) {
        options = buf.readVarInt()
        pos = buf.readNullable(PacketByteBuf::readBlockPos)
    }
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(options)
        buf.writeNullable(pos, PacketByteBuf::writeBlockPos)
    }

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
