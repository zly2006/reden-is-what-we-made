package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class Pause(
    val paused: Boolean
) : CustomPayload {
    companion object {
        val ID = CustomPayload.Id<Pause>(Reden.identifier("pause"))
        val CODEC = PacketCodec.ofStatic<PacketByteBuf, Pause>({ buf, obj ->
            buf.writeBoolean(obj.paused)
        }, { buf ->
            Pause(buf.readBoolean()).apply {
                // Note: fire event in network thread, so our packet can be processed immediately
                if (server.isOnThread) {
                    Reden.LOGGER.warn("Pause packet is processed on main thread, this may cause it not processed immediately")
                }
                server.data.tickStageTree.stepInto {
                    server.sendToAll(Pause(true))
                }
            }
        })
        fun register() {
            PayloadTypeRegistry.playC2S().register(ID, CODEC)
            ServerPlayNetworking.registerGlobalReceiver(ID) { _, _ ->
                // NOOP
            }
        }
    }

    override fun getId() = ID
}
