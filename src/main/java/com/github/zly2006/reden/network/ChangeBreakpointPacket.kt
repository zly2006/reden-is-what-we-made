package com.github.zly2006.reden.network

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

private val id = Identifier("reden", "change_breakpoint")
private val pType = PacketType.create(id) {
    val bp = BreakPoint.read(it)
    val flag = it.readVarInt()
    val bpId = it.readVarInt()
    ChangeBreakpointPacket(bp, flag, bpId)
}

class ChangeBreakpointPacket(
    val breakPoint: BreakPoint,
    val flag: Int = 0,
    val bpId: Int = 0,
): FabricPacket {
    companion object {
        const val ADD = 1
        const val REMOVE = 2
        const val DISABLE = 4
    }

    override fun write(buf: PacketByteBuf) {
        breakPoint.write(buf)
        buf.writeVarInt(flag)
        buf.writeVarInt(bpId)
    }

    override fun getType(): PacketType<ChangeBreakpointPacket> = pType
}