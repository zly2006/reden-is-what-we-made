package com.github.zly2006.reden.access

import com.github.zly2006.reden.debugger.breakpoint.BreakpointsManager
import com.github.zly2006.reden.debugger.stages.ServerRootStage
import com.github.zly2006.reden.debugger.tree.TickStageTree
import com.github.zly2006.reden.network.GlobalStatus
import com.github.zly2006.reden.transformers.sendToAll
import com.github.zly2006.reden.utils.codec.FabricVersionSerializer
import com.github.zly2006.reden.utils.codec.UUIDSerializer
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import okhttp3.internal.toHexString
import java.util.*

@Serializable
class ServerData(
    @Serializable(FabricVersionSerializer::class)
    val version: Version
) : StatusAccess {
    constructor(version: Version, mcServer: MinecraftServer) : this(version) {
        server = mcServer
        serverId = mcServer.session.directory.path().hashCode().toHexString()
        breakpoints = BreakpointsManager(false)
    }

    @JvmField var realTicks = 0
    override var status: Long = 0

    @Serializable(UUIDSerializer::class)
    var uuid: UUID? = null
    var serverId = ""
    var address: String = ""

    @Transient
    var tickStage: ServerRootStage? = null

    @Transient
    var tickStageTree = TickStageTree()
    var worlds: MutableList<WorldData> = mutableListOf()

    var frozen: Boolean
        @JvmName("isFrozen")
        get() = hasStatus(GlobalStatus.FROZEN)
        set(value) {
            if (value) addStatus(GlobalStatus.FROZEN) else removeStatus(GlobalStatus.FROZEN)
        }

    fun freeze(reason: String) {
        frozen = true
        GlobalStatus.packet(server.data.status, NbtCompound().apply {
            putString("reason", reason)
        }).let(server::sendToAll)
    }

    val featureSet = mutableSetOf<String>()

    @Transient
    var breakpoints = BreakpointsManager(true)

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
        }
        else {
            val mc = MinecraftClient.getInstance()
            if (mc.isInSingleplayer) mc.server?.data
            else mc.serverData
        }
    }

}
