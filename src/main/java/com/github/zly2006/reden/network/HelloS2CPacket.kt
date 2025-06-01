package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.network.HelloS2CPacket.Companion.CODEC
import com.github.zly2006.reden.network.HelloS2CPacket.Companion.ID
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

@Serializable
class HelloS2CPacket(
    val versionString: String,
    val featureSet: Set<String>,
) : CustomPacketPayload {
    override fun type() = ID

    companion object : PacketCodecHelper<HelloS2CPacket> by PacketCodec(Reden.identifier("hello_s2c"))
}

fun registerHello() {
    PayloadTypeRegistry.configurationS2C().register(ID, CODEC)
    ServerConfigurationConnectionEvents.CONFIGURE.register { handler, _ ->
        ServerConfigurationNetworking.send(
            handler, HelloS2CPacket(
                Reden.MOD_VERSION, setOf(
                    "reden",
                    "undo",
                 )
            )
        )
    }
}
