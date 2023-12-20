package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.stages.ServerRootStage
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.network.GlobalStatus
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
    var tickStage: ServerRootStage? = null
    var tickStageTree = TickStageTree()

    var frozen: Boolean
        @JvmName("isFrozen")
        get() = hasStatus(GlobalStatus.FROZEN)
        set(value) {
            if (value) addStatus(GlobalStatus.FROZEN) else removeStatus(GlobalStatus.FROZEN)
        }
    val featureSet = mutableSetOf<String>()

    val breakpoints = BreakpointsManager(false)

    interface ServerDataAccess {
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getServerData\$reden")
        val serverData: ServerData
    }

    interface ClientSideServerDataAccess {
        @Suppress("INAPPLICABLE_JVM_NAME")
        @get:JvmName("getServerData\$reden")
        @set:JvmName("setServerData\$reden")
        var serverData: ServerData?
    }

    companion object {
        @JvmStatic
        val MinecraftServer.data: ServerData
            get() = (this as ServerDataAccess).serverData
        val MinecraftClient.serverData: ServerData?
            get() = (this as ClientSideServerDataAccess).serverData
        @JvmStatic
        fun getServerData() = if (!isClient) {
            server.data
        } else {
            val mc = MinecraftClient.getInstance()
            if (mc.isInSingleplayer) mc.server?.data
            else mc.serverData
        }
    }

}