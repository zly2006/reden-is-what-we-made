package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.debugger.unfreeze
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class Continue: FabricPacket {
    companion object {
        val id = Reden.identifier("continue")
        val pType = PacketType.create(id) {
            Continue()
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { _, player, _ ->
                unfreeze(player.server)
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        // empty
    }

    override fun getType() = pType
}
