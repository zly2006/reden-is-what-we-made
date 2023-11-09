package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf

class Hello(
    val version: Version
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(version.toString())
    }
    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = Reden.identifier("hello")
        private val pType = PacketType.create(id) {
            Hello(Version.parse(it.readString()))
        }
        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                    Reden.LOGGER.info("Hello from server: ${packet.version}")
                    val mc = MinecraftClient.getInstance()
                    (mc as ServerData.ClientSideServerDataAccess).redenServerData = ServerData(null)
                }
            }
            ServerPlayConnectionEvents.JOIN.register { _, sender, _ ->
                sender.sendPacket(Hello(Reden.MOD_VERSION))
            }
        }
    }
}
