package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.network.StageTreeS2CPacket
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.server

class PauseGame: BreakPointBehavior() {
    override fun onBreakPoint(breakPoint: BreakPoint, event: Any) {
        val tree = server.data().tickStageTree
        server.sendToAll(StageTreeS2CPacket(tree))
        server.sendToAll(GlobalStatus(server.data().status, null))
    }
}