package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

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
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                val data = player.server.data()
                if (data.tickStageTree.hasNext()) {
                    data.tickStageTree.next().tick()
                    sender.sendPacket(StepOver(true))
                    sender.sendPacket(StageTreeS2CPacket(data.tickStageTree))
                }
                else {
                    sender.sendPacket(StepOver(false))
                }
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->

                }
            }
        }
    }
}
