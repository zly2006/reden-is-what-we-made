package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.rvc.RvcManager
import com.github.zly2006.reden.wormhole.Wormhole
import net.minecraft.client.MinecraftClient
import net.minecraft.network.NetworkSide

class ClientData(
    @get:JvmName("mc") val mc: MinecraftClient
): StatusAccess {
    override var status: Long = 0
    val breakpoints = BreakpointsManager(true)
    val lastTriggeredBreakpoint: BreakPoint? = null
    val wormholes = mutableListOf<Wormhole>()
    val rvc = RvcManager(NetworkSide.CLIENTBOUND)

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
