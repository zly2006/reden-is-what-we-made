package com.github.zly2006.reden.network

import com.github.zly2006.reden.access.ChainedUpdaterView
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.server
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier

private val id = Identifier("render", "step_update")
private val pType = PacketType.create(id) {
    val times = it.readVarInt()
    val untilSuppression = it.readBoolean()
    StepUpdate(times, untilSuppression)
}

class StepUpdate(
    val times: Int,
    val untilSuppression: Boolean,
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(times)
        buf.writeBoolean(untilSuppression)
    }

    companion object {
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