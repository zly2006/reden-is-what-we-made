package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.unfreeze
import com.github.zly2006.reden.utils.red
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class Continue: FabricPacket {
    companion object {
        val id = Reden.identifier("continue")
        val pType = PacketType.create(id) {
            Continue()
        }!!

        fun <T> T.checkFrozen(player: ServerPlayerEntity, action: T.() -> Unit) {
            if (player.server.data.hasStatus(GlobalStatus.FROZEN))
                action()
            else
                player.sendMessage(Text.literal("The game is not frozen").red())
        }

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { _, player, _ ->
                checkFrozen(player) {
                    unfreeze(player.server)
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        // empty
    }

    override fun getType() = pType
}
