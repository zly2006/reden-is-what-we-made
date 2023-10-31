package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import net.minecraft.client.MinecraftClient

class ClientData(
    val mc: MinecraftClient
): StatusAccess {
    override var status: Long = 0
    val breakpoints = BreakpointsManager()

    interface ClientDataAccess {
        fun getRedenClientData(): ClientData
    }

    companion object {
        @JvmStatic
        fun MinecraftClient.data(): ClientData {
            return (this as ClientDataAccess).getRedenClientData()
        }
    }
}