package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.unfreeze
import com.github.zly2006.reden.utils.red
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

@Serializable
class Continue : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<Continue> by PacketCodec(Reden.identifier("continue")) {
        fun <T> T.checkFrozen(player: ServerPlayerEntity, action: T.() -> Unit) {
            if (player.server.data.frozen)
                action()
            else
                player.sendMessage(Text.literal("The game is not frozen").red())
        }

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(ID) { _, context ->
                checkFrozen(context.player()) {
                    unfreeze(context.player().server)
                }
            }
        }
    }
}
