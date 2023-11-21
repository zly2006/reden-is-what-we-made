package com.github.zly2006.reden.debugger.stages.world

import com.github.zly2006.reden.access.WorldTickSchedulerAccess
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import net.minecraft.fluid.Fluid
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.tick.OrderedTick
import net.minecraft.world.tick.TickPriority

class FluidScheduledTickStage(
    val _parent: FluidScheduledTicksRootStage,
    var orderedTick: OrderedTick<Fluid>?,
): TickStage("fluid_scheduled_tick", _parent), TickStageWithWorld {
    override val world: ServerWorld get() = _parent.world

    @Suppress("UNCHECKED_CAST")
    // Note: tick() method that does not call TickStage#tick
    override fun tick() {
        assert(children.isEmpty())
        val scheduler = _parent.world.fluidTickScheduler
        scheduler as WorldTickSchedulerAccess<Fluid>
        scheduler.setTickingTick(orderedTick!!)
        scheduler.tick(_parent.world::tickFluid)
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
