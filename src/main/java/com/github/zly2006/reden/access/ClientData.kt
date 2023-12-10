package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import net.minecraft.client.MinecraftClient

class ClientData(
    val mc: MinecraftClient
): StatusAccess {
    override var status: Long = 0
    val breakpoints = BreakpointsManager(true)

    interface ClientDataAccess {
        fun `getClientData$reden`(): ClientData
    }

    companion object {
        @JvmStatic
        fun MinecraftClient.data(): ClientData {
            return (this as ClientDataAccess).`getClientData$reden`()
        }
    }
}