package com.github.zly2006.reden.access

import net.minecraft.client.MinecraftClient

class ClientData(
    @get:JvmName("mc") val mc: MinecraftClient
): StatusAccess {
    override var status: Long = 0

    @Suppress("INAPPLICABLE_JVM_NAME")
    interface ClientDataAccess {
        @get:JvmName("getClientData\$reden")
        val clientData: ClientData
    }

    companion object {
        @JvmStatic
        val MinecraftClient.data: ClientData get() {
            return (this as ClientDataAccess).clientData
        }
    }
}
