package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
import com.github.zly2006.reden.debugger.stages.world.*
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class WorldRootStage(
    override val world: ServerWorld?,
    parent: TickStage,
    var identifier: Identifier?
) : TickStage("world_root", parent = parent), TickStageWithWorld {
    @Deprecated("TickStage is going not to be tickable.")
    fun tick() {
        children.add(WorldBorderStage(this))
        children.add(WeatherStage(this))
        children.add(TimeStage(this))
        children.add(BlockScheduledTicksRootStage(this))
        children.add(FluidScheduledTicksRootStage(this))
        children.add(RaidStage(this))
        //todo: spawn stage and random tick stage
        // profiler.swap("chunkSource");
        postTick()
        children.add(RandomTickStage(this))
        children.add(BlockEventsRootStage(this))
        children.add(EntitiesRootStage(this))
        children.add(BlockEntitiesRootStage(this))
    }

    override val displayName: MutableText get() =
        Text.translatable(
            "reden.debugger.tick_stage.world_root",
            Text.translatable("reden.constants.world.${identifier!!.path}")
        )

    override val description: MutableText
        get() = Text.of(identifier).copy()

    override fun toString() = "World RootStage/${identifier}"

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        identifier = buf.readIdentifier()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeIdentifier(identifier!!)
    }
}
