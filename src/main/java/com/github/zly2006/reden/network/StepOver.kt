package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.Continue.Companion.checkFrozen
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.red
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

class StepOver(
    val success: Boolean,
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeBoolean(success)
    }

    companion object {
        val id = Reden.identifier("step_over")
        val pType = PacketType.create(id) {
            val untilSuppression = it.readBoolean()
            StepOver(untilSuppression)
        }!!
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { _, player, sender ->
                checkFrozen(player) {
                    try {
                        val tree = player.server.data().tickStageTree

                        if (tree.activeStage == null ||
                            !tree.stepOver(tree.activeStage!!) {
                                sender.sendPacket(BreakPointInterrupt(-2, tree, true))
                            }
                        ) player.sendMessage(Text.literal("Failed to step over: no more stages.").red())
                    } catch (e: Exception) {
                        player.sendMessage(Text.literal("Failed to step over.").red())
                        Reden.LOGGER.error("There is something wrong, but it is not your bad.", e)
                    }
                }
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->

                }
            }
        }
    }
}
