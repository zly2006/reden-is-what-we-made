package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.Continue.Companion.checkFrozen
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.red
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.packet.CustomPayload
import net.minecraft.text.Text

@Serializable
class StepOver(
    /**
     * Step over, until the stage is ticked.
     */
    val stageId: Int,
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<StepOver> by PacketCodec(Reden.identifier("step_over")) {
        fun register() {
            PayloadTypeRegistry.playC2S().register(ID, CODEC)
            ServerPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                checkFrozen(context.player()) {
                    try {
                        val tree = context.player().server.data.tickStageTree
                        val target = tree.activeStages.firstOrNull { it.id == packet.stageId }

                        if (target == null || !tree.stepOver(target) {
                                context.responseSender().sendPacket(BreakPointInterrupt(-2, tree, true))
                            }
                        ) context.player().sendMessage(Text.literal("Failed to step over: stage not found.").red())
                    } catch (e: Exception) {
                        context.player().sendMessage(Text.literal("Failed to step over.").red())
                        Reden.LOGGER.error("There is something wrong, but it is not your bad.", e)
                    }
                }
            }
            if (isClient) {
                PayloadTypeRegistry.playS2C().register(ID, CODEC)
                ClientPlayNetworking.registerGlobalReceiver(ID) { packet, _ ->

                }
            }
        }
    }
}
