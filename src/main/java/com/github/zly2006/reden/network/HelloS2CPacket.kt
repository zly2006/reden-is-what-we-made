package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.access.TransferCooldownAccess
import com.github.zly2006.reden.network.HelloS2CPacket.Companion.CODEC
import com.github.zly2006.reden.network.HelloS2CPacket.Companion.ID
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.translateMessage
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload

@Serializable
class HelloS2CPacket(
    val versionString: String,
    val featureSet: Set<String>,
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<HelloS2CPacket> by PacketCodec(Reden.identifier("hello_s2c"))
}

fun registerHello() {
    PayloadTypeRegistry.configurationS2C().register(ID, CODEC)
    ServerConfigurationConnectionEvents.CONFIGURE.register { handler, _ ->
        ServerConfigurationNetworking.send(
            handler, HelloS2CPacket(
                Reden.MOD_VERSION.friendlyString, setOf(
                    "reden",
                    "undo",
                    "hopper-cd",
                    "experimental:debugger",
                    "experimental:pearl",
                )
            )
        )
    }
    if (isClient) {
        ClientConfigurationNetworking.registerGlobalReceiver(ID) { packet, _ ->
            Reden.LOGGER.info("Hello from server: ${packet.versionString}")
            Reden.LOGGER.info("Feature set: " + packet.featureSet.joinToString())
            (MinecraftClient.getInstance() as ServerData.ClientSideServerDataAccess).serverData =
                ServerData(Version.parse(packet.versionString)).apply {
                    featureSet.addAll(packet.featureSet)
                }
        }
        ClientPlayConnectionEvents.INIT.register { _, mc ->
            mc.serverData?.featureSet?.forEach { name ->
                when (name) {
                    "hopper-cd" -> ClientPlayNetworking.registerReceiver(HopperCDSync.ID) { packet, _ ->
                        val screen = MinecraftClient.getInstance().currentScreen
                        if (screen is TransferCooldownAccess) {
                            screen.transferCooldown = packet.cd
                        }
                        HopperCDSync.currentDelay = packet.cd
                        HopperCDSync.currentPos = packet.pos
                    }

                    "undo"      -> ClientPlayNetworking.registerReceiver(Undo.ID) { packet, context ->
                        context.player().sendMessage(
                            when (packet.status) {
                                0     -> translateMessage("undo", "rollback_success")
                                1     -> translateMessage("undo", "restore_success")
                                2     -> translateMessage("undo", "no_blocks_info")
                                16    -> translateMessage("undo", "no_permission")
                                32    -> translateMessage("undo", "not_recording")
                                64    -> translateMessage("undo", "busy")
                                65536 -> translateMessage("undo", "unknown_error")
                                else  -> translateMessage("undo", "unknown_status")
                            }
                        )
                    }
                }
            }
        }
    }
}
