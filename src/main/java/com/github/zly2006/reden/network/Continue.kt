package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.unfreeze
import com.github.zly2006.reden.utils.red
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

class Continue: FabricPacket {
    companion object {
        val id = Reden.identifier("continue")
        val pType = PacketType.create(id) {
            Continue()
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { _, player, _ ->
                if (player.server.data().hasStatus(GlobalStatus.FROZEN))
                    unfreeze(player.server)
                else
                    player.sendMessage(Text.literal("The game is not frozen").red())
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        // empty
    }

    override fun getType() = pType
}
