package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.multiver.Text
import fi.dy.masa.malilib.util.StringUtils
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.network.chat.Component

private const val MESSAGE_PREFIX = "${Reden.MOD_ID}.message."

fun translateMessage(category: String, key: String, vararg args: Any): Component {
    val msg = StringUtils.translate("$MESSAGE_PREFIX$category.$key", args)
    return Text.translatable("$MESSAGE_PREFIX$category.base", msg)
}

fun registerClientPackets() {
    ClientConfigurationNetworking.registerGlobalReceiver(HelloS2CPacket.ID) { packet, context ->
        Reden.LOGGER.info("Hello from server: $packet")
        Reden.LOGGER.info("Feature set: " + packet.featureSet.joinToString())
        packet.featureSet.forEach { name ->
            when (name) {
                "undo" -> ClientPlayNetworking.registerGlobalReceiver(Undo.ID) { packet, context ->
                    context.player().sendSystemMessage(
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
