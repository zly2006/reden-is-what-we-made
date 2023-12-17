package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.fluid.Fluid
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.world.tick.OrderedTick
import net.minecraft.world.tick.TickPriority

class FluidScheduledTickStage(
    val _parent: FluidScheduledTicksRootStage,
    var orderedTick: OrderedTick<Fluid>?,
): TickStage("fluid_scheduled_tick", _parent), TickStageWithWorld {
    override val world get() = _parent.world

    override fun preTick() {
        world!!.server.data.breakpoints.checkBreakpointsForScheduledTick()
        super.preTick()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeIdentifier(Registries.FLUID.getId(orderedTick!!.type))
        buf.writeBlockPos(orderedTick!!.pos)
        buf.writeLong(orderedTick!!.triggerTick)
        buf.writeEnumConstant(orderedTick!!.priority)
        buf.writeLong(orderedTick!!.subTickOrder)
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        val type = Registries.FLUID.get(buf.readIdentifier())
        orderedTick = OrderedTick(
            type,
            buf.readBlockPos(),
            buf.readLong(),
            buf.readEnumConstant(TickPriority::class.java),
            buf.readLong(),
        )
    }
}
