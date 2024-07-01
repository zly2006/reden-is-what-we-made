package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.Continue.Companion.checkFrozen
import com.github.zly2006.reden.utils.red
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.packet.CustomPayload
import net.minecraft.text.Text

@Serializable
class StepInto : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<StepInto> by PacketCodec(Reden.identifier("step_into")) {
        fun register() {
            PayloadTypeRegistry.playC2S().register(ID, CODEC)
            ServerPlayNetworking.registerGlobalReceiver(ID) { packet, context ->
                checkFrozen(context.player()) {
                    try {
                        val tree = context.player().server.data.tickStageTree
                        if (tree.activeStage != null) {
                            tree.stepInto {
                                context.responseSender().sendPacket(BreakPointInterrupt(-1, tree, true))
                            }
                        } else {
                            context.player().sendMessage(Text.literal("Failed to step into: no more stages.").red())
                        }
                    }
                    catch (e: Exception) {
                        context.player().sendMessage(Text.literal("Failed to step into.").red())
                        Reden.LOGGER.error("There is something wrong, but it is not your bad.", e)
                    }
                }
            }
        }
    }
}
