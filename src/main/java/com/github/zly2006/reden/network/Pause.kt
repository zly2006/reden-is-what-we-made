package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.transformers.sendToAll
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class Pause(
    val paused: Boolean
): FabricPacket {
    companion object {
        val id = Reden.identifier("pause")
        val pType = PacketType.create(id) {
            Pause(it.readBoolean())
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                if (!packet.paused) return@registerGlobalReceiver
                player.server.data.tickStageTree.stepInto {
                    player.server.sendToAll(Pause(true))
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
    }

    override fun getType() = pType
}
