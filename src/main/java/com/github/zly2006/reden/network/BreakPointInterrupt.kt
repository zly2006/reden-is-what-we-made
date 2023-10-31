package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.tree.StageIo
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

data class BreakPointInterrupt(
    val bpId: Int,
    val tree: StageTree?,
    val interrupted: Boolean = true
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(bpId)
        buf.writeNullable(tree) { _, tree ->
            StageIo.writeStageTree(tree, buf)
        }
    }

    override fun getType(): PacketType<BreakPointInterrupt> = pType

    companion object {
        val id = Reden.identifier("breakpoint_interrupt")
        val pType = PacketType.create(id) {
            val id = it.readVarInt()
            val tree = it.readNullable(StageIo::readStageTree)
            BreakPointInterrupt(id, tree)
        }!!
        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                }
            }
        }
    }
}