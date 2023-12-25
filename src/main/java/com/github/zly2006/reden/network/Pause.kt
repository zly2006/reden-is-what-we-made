package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.server
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
            Pause(it.readBoolean()).apply {
                // Note: fire event in network thread, so our packet can be processed immediately
                if (server.isOnThread) {
                    Reden.LOGGER.warn("Pause packet is processed on main thread, this may cause it not processed immediately")
                }
                server.data.tickStageTree.stepInto {
                    server.sendToAll(Pause(true))
                }
            }
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { _, _, _ ->
                // NOOP
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
    }

    override fun getType() = pType
}
