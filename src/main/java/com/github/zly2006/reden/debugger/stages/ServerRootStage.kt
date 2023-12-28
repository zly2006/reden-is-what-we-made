package com.github.zly2006.reden.debugger.stages

import com.github.zly2006.reden.debugger.TickStage
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text

class ServerRootStage(
    val server: MinecraftServer?,
    var ticks: Int = -1
): TickStage(
    "server_root", null,
    Text.literal("Server Tick"),
    Text.literal("The root of a server tick")
) {
    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        ticks = buf.readVarInt()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeVarInt(ticks)
    }

    override fun toString() = "Server RootStage"
}
