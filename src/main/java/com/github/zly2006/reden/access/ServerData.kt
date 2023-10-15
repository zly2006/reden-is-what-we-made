package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.ServerRootStage
import com.github.zly2006.reden.utils.server
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer
import java.util.*

class ServerData {
    var status: Long = 0
    var uuid: UUID? = null
    var address: String = ""
    val tickStage = ServerRootStage(server)
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