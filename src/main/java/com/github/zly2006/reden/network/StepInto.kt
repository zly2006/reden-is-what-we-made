package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

class StepInto: FabricPacket {
    override fun write(buf: PacketByteBuf) {
    }

    override fun getType() = pType

    companion object {
        val id = Reden.identifier("step_into")
        val pType = PacketType.create(id) {
            StepInto()
        }!!
        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->

            }
        }
    }
}