package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.debugger.gui.DebuggerScreen
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.red
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload
import net.minecraft.text.Text

data class BreakPointInterrupt(
    val bpId: Int, // = -1
    val tree: TickStageTree?,
    val interrupted: Boolean
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<BreakPointInterrupt> by PacketCodec(Reden.identifier("breakpoint_interrupt")) {
        fun register() {
            if (isClient) {
                PayloadTypeRegistry.playS2C().register(ID, CODEC)
                ClientPlayNetworking.registerGlobalReceiver(ID) { packet, _ ->
                    val data = MinecraftClient.getInstance().serverData!!
                    val breakpoint = data.breakpoints.breakpointMap[packet.bpId]
                    if (packet.tree != null) {
                        data.tickStageTree = packet.tree
                    }
                    val mc = MinecraftClient.getInstance()
                    if (packet.interrupted) {
                        mc.setScreen(DebuggerScreen(data.tickStageTree, breakpoint))
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
