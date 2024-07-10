package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.gui.DebuggerComponent
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import io.wispforest.owo.ui.hud.Hud
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload

@Serializable
class StageTreeS2CPacket(
    @Contextual
    val tree: TickStageTree
) : CustomPayload {
    companion object : PacketCodecHelper<StageTreeS2CPacket> by PacketCodec(Reden.identifier("stage_tree_s2c")) {
        fun register() {
            PayloadTypeRegistry.playS2C().register(ID, CODEC)
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                    val mc = MinecraftClient.getInstance()
                    context.player().sendMessage("Tick stage tree")
                    Hud.remove(Reden.identifier("debugger"))
                    Hud.add(Reden.identifier("debugger")) {
                        DebuggerComponent(packet.tree).asHud()
                    }
                }
            }
        }
    }

    override fun getId() = ID
}
