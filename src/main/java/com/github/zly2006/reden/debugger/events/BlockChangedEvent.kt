package com.github.zly2006.reden.debugger.events

import com.github.zly2006.reden.access.ServerData.Companion.data
import com.github.zly2006.reden.network.StageTreeS2CPacket
import com.github.zly2006.reden.utils.sendMessage
import com.github.zly2006.reden.utils.server
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class BlockChangedEvent(
    val pos: BlockPos,
    val oldState: BlockState,
    val newState: BlockState
) {
    fun fire() {
        if (pos == BlockPos.ORIGIN) {
            //todo: waiting for breakpoints
            server.playerManager.playerList.forEach {
                it.sendMessage("Block changed at origin")
                ServerPlayNetworking.send(it, StageTreeS2CPacket(server.data().tickStageTree))
            }
        }
    }
}
