package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.debugger.TickStageWithWorld
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
