package com.github.zly2006.reden.access

import net.minecraft.server.MinecraftServer

class ServerData {
    var status: Long = 0
    interface ServerDataAccess {
        fun getRedenServerData(): ServerData
    }

    companion object {
        fun MinecraftServer.data(): ServerData {
            return (this as ServerDataAccess).getRedenServerData()
        }
    }
}