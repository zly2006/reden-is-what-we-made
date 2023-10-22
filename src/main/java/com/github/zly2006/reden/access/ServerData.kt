package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.stages.ServerRootStage
import com.github.zly2006.reden.debugger.tree.StageTree
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer
import java.util.*

class ServerData(
    server: MinecraftServer
) {
    var status: Long = 0
    var uuid: UUID? = null
    var address: String = ""
    val tickStage = ServerRootStage(server)
    var tickStageTree = StageTree().apply {
        root = StageTree.TreeNode(null, tickStage, false, null)
    }
    interface ServerDataAccess {
        fun getRedenServerData(): ServerData
    }

    interface ClientSideServerDataAccess {
        fun getRedenServerData(): ServerData
    }

    companion object {
        fun MinecraftServer.data(): ServerData {
            return (this as ServerDataAccess).getRedenServerData()
        }
        fun MinecraftClient.serverData(): ServerData {
            return (this as ClientSideServerDataAccess).getRedenServerData()
        }
    }
}