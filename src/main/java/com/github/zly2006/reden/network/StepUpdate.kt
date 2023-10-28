package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ChainedUpdaterView
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class StepUpdate(
    val times: Int,
    val untilUpdateSuppression: Boolean,
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(times)
        buf.writeBoolean(untilUpdateSuppression)
    }

    companion object {
        val id = Reden.identifier("step_update")
        val pType = PacketType.create(id) {
            val times = it.readVarInt()
            val untilSuppression = it.readBoolean()
            StepUpdate(times, untilSuppression)
        }
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                val bph = (player.world.neighborUpdater as? ChainedUpdaterView)?.breakpointHelper
                if (bph == null || !bph.isInterrupted) {
                    sender.sendPacket(StepUpdate(-1, false))
                }
                else {
                    val status = player.world.data()!!.removeStatus(WorldStatus.FROZEN)
                    server.playerManager.playerList.forEach {
                        ServerPlayNetworking.send(it, WorldStatus(status, null))
                    }
                }
            }
        }
    }
}