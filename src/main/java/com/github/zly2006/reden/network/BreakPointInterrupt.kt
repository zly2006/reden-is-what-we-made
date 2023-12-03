package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.debugger.gui.DebuggerComponent
import com.github.zly2006.reden.debugger.tree.StageIo
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.red
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

data class BreakPointInterrupt(
    val bpId: Int, // = -1
    val tree: StageTree?,
    val interrupted: Boolean
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(bpId)
        buf.writeNullable(tree) { _, tree ->
            StageIo.writeStageTree(tree, buf)
        }
        buf.writeBoolean(interrupted)
    }

    override fun getType() = pType

    companion object {
        val id = Reden.identifier("breakpoint_interrupt")
        val pType = PacketType.create(id) {
            BreakPointInterrupt(
                it.readVarInt(),
                it.readNullable(StageIo::readStageTree),
                it.readBoolean()
            )
        }!!
        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, _ ->
                    val data = MinecraftClient.getInstance().serverData()!!
                    val breakpoint = data.breakpoints.breakpointMap[packet.bpId]
                    if (packet.tree != null) {
                        data.tickStageTree = packet.tree
                    }
                    val mc = MinecraftClient.getInstance()
                    if (packet.interrupted) {
                        mc.setScreen(DebuggerComponent(data.tickStageTree).asScreen())
                        mc.messageHandler.onGameMessage(Text.literal("Game was frozen, any updates will be blocked").red(), true)
                        mc.inGameHud.overlayRemaining = 100000
                    }
                    else {
                        mc.inGameHud.overlayRemaining = 0
                    }
                }
            }
        }
    }
}