package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.Continue.Companion.checkFrozen
import com.github.zly2006.reden.utils.red
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

class StepInto: FabricPacket {
    override fun write(buf: PacketByteBuf) {
    }

    override fun getType() = pType

    companion object {
        val id = Reden.identifier("step_into")
        val pType = PacketType.create(id) {
            StepInto()
        }!!
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                checkFrozen(player) {
                    try {
                        val tree = player.server.data().tickStageTree
                        if (tree.activeStage != null) {
                            tree.stepInto {
                                sender.sendPacket(BreakPointInterrupt(-1, tree, true))
                            }
                        } else {
                            player.sendMessage(Text.literal("Failed to step into: no more stages.").red())
                        }
                    }
                    catch (e: Exception) {
                        player.sendMessage(Text.literal("Failed to step into.").red())
                        Reden.LOGGER.error("There is something wrong, but it is not your bad.", e)
                    }
                }
            }
        }
    }
}