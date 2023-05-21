package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.network.BreakPointInterrupt
import com.github.zly2006.reden.server
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

class Interrupt: BreakPointBehavior() {
    override fun onBreakPoint(breakPoint: BreakPoint) {
        server.playerManager.playerList.forEach {
            ServerPlayNetworking.send(it, BreakPointInterrupt(breakPoint.id))
        }
    }
}