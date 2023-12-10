package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData
import com.github.zly2006.reden.access.TransferCooldownAccess
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.translateMessage
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf

class Hello(
    val version: Version,
    val featureSet: Set<String>,
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeString(version.toString())
        buf.writeVarInt(featureSet.size)
        featureSet.forEach {
            buf.writeString(it)
        }
    }
    override fun getType(): PacketType<*> = pType

    companion object {
        private val id = Reden.identifier("hello")
        private val pType = PacketType.create(id) { buf ->
            val version = Version.parse(buf.readString())
            val featureSet = mutableSetOf<String>()
            repeat(buf.readVarInt()) {
                featureSet.add(buf.readString())
            }
            Hello(version, featureSet)
        }
        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                    Reden.LOGGER.info("Hello from server: ${packet.version}")
                    Reden.LOGGER.info("Feature set: " + packet.featureSet.joinToString())
                    val mc = MinecraftClient.getInstance()
                    (mc as ServerData.ClientSideServerDataAccess).`serverData$reden` =
                        ServerData(packet.version, null).apply {
                            featureSet.addAll(packet.featureSet)
                        }
                    packet.featureSet.forEach { name ->
                        when (name) {
                            "hopper-cd" -> ClientPlayNetworking.registerReceiver(HopperCDSync.pType) { packet, _, _ ->
                                val screen = MinecraftClient.getInstance().currentScreen
                                if (screen is TransferCooldownAccess) {
                                    screen.`transferCooldown$reden` = packet.cd
                                }
                                HopperCDSync.currentDelay = packet.cd
                                HopperCDSync.currentPos = packet.pos
                            }
                            "undo" -> ClientPlayNetworking.registerReceiver(Undo.pType) { packet, player, _ ->
                                player.sendMessage(
                                    when (packet.status) {
                                        0 -> translateMessage("undo", "rollback_success")
                                        1 -> translateMessage("undo", "restore_success")
                                        2 -> translateMessage("undo", "no_blocks_info")
                                        16 -> translateMessage("undo", "no_permission")
                                        32 -> translateMessage("undo", "not_recording")
                                        64 -> translateMessage("undo", "busy")
                                        65536 -> translateMessage("undo", "unknown_error")
                                        else -> translateMessage("undo", "unknown_status")
                                    }
                                )
                            }
                        }
                    }
                }
            }
            ServerPlayConnectionEvents.JOIN.register { _, sender, _ ->
                sender.sendPacket(Hello(Reden.MOD_VERSION, setOf(
                    "reden",
                    "undo",
                    "hopper-cd",
                    "experimental:debugger",
                    "experimental:pearl",
                )))
            }
        }
    }
}
