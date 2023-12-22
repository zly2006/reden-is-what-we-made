package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import net.minecraft.client.MinecraftClient

class ClientData(
    val mc: MinecraftClient
): StatusAccess {
    override var status: Long = 0
    val breakpoints = BreakpointsManager(true)
    val lastTriggeredBreakpoint: BreakPoint? = null

    interface ClientDataAccess {
        @Suppress("INAPPLICABLE_JVM_NAME")
        @JvmName("getClientData\$reden")
        fun clientData(): ClientData
    }

    companion object {
        @JvmStatic
        fun MinecraftClient.data(): ClientData {
            return (this as ClientDataAccess).clientData()
        }
    }
}