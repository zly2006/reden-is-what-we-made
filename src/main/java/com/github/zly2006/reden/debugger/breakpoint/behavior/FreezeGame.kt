package com.github.zly2006.reden.debugger.breakpoint.behavior

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BreakPoint
import com.github.zly2006.reden.debugger.tickPackets
import com.github.zly2006.reden.network.BreakPointInterrupt
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.server
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Util

class FreezeGame: BreakPointBehavior() {
    override val id = Reden.identifier("freeze_game_behavior")
    init {
        defaultPriority = 100
    }
    override fun onBreakPoint(breakPoint: BreakPoint, event: Any) {
        val tree = server.data.tickStageTree
        server.sendToAll(BreakPointInterrupt(breakPoint.id, tree, true))

        server.data.addStatus(GlobalStatus.FROZEN)
            .let {
                GlobalStatus.packet(it, NbtCompound().apply {
                    putString("reason", "game-paused")
                })
            }.let(server::sendToAll)

        while (server.data.frozen && server.isRunning) {
            tickPackets(server)
        }


        server.tickStartTimeNanos = Util.getMeasuringTimeMs()
        server.sendToAll(BreakPointInterrupt(breakPoint.id, null, false))
    }
}
