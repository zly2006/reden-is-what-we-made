package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.stages.ServerRootStage
import com.github.zly2006.reden.debugger.tree.StageTree
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.server.MinecraftServer
import java.util.*

class ServerData(version: Version, mcServer: MinecraftServer?) : StatusAccess {
    init {
        if (mcServer != null) {
            server = mcServer
        }
    }
    @JvmField var realTicks = 0
    override var status: Long = 0
    var uuid: UUID? = null
    var address: String = ""
    var tickStage = if (mcServer != null) ServerRootStage(mcServer) else null
    var tickStageTree = StageTree()
    val featureSet = mutableSetOf<String>()

    val breakpoints = BreakpointsManager(false)

    interface ServerDataAccess {
        val `serverData$reden`: ServerData
    }

    interface ClientSideServerDataAccess {
        var `serverData$reden`: ServerData?
    }

    companion object {
        @JvmStatic
        fun MinecraftServer.data(): ServerData {
            return (this as ServerDataAccess).`serverData$reden`
        }
        fun MinecraftClient.serverData(): ServerData? {
            return (this as ClientSideServerDataAccess).`serverData$reden`
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