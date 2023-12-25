package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.fluid.Fluid
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class FluidScheduledTickStage(
    val _parent: FluidScheduledTicksRootStage,
    var pos: BlockPos?,
    var fluid: Fluid?
): TickStage("fluid_scheduled_tick", _parent), TickStageWithWorld {
    override val displayName: MutableText
        get() = Text.translatable("reden.debugger.tick_stage.fluid_scheduled_tick", pos?.toShortString())

    override val world get() = _parent.world

    override fun preTick() {
        world!!.server.data.breakpoints.checkBreakpointsForScheduledTick()
        super.preTick()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(pos)
        buf.writeIdentifier(Registries.FLUID.getId(fluid))
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        pos = buf.readBlockPos()
        fluid = Registries.FLUID.get(buf.readIdentifier())
    }
}
