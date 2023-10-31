package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.stages.ServerRootStage
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer
import java.util.*

class ServerData(
    server: MinecraftServer
): StatusAccess {
    @JvmField var realTicks = 0
    override var status: Long = 0
    var uuid: UUID? = null
    var address: String = ""
    val tickStage = ServerRootStage(server)
    var tickStageTree = StageTree()

    val breakpoints = BreakpointsManager()

    interface ServerDataAccess {
        fun getRedenServerData(): ServerData
    }

    interface ClientSideServerDataAccess {
        var redenServerData: ServerData?
    }

    companion object {
        @JvmStatic
        fun MinecraftServer.data(): ServerData {
            return (this as ServerDataAccess).getRedenServerData()
        }
        fun MinecraftClient.serverData(): ServerData? {
            return (this as ClientSideServerDataAccess).redenServerData
        }
        @JvmStatic
        fun getServerData() = if (!isClient) {
            server.data()
        } else {
            val mc = MinecraftClient.getInstance()
            if (mc.isInSingleplayer) mc.server?.data()
            else mc.serverData()
        }
    }
}