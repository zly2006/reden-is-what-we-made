package com.github.zly2006.reden.access

import com.github.zly2006.reden.utils.codec.FabricVersionSerializer
import com.github.zly2006.reden.utils.codec.UUIDSerializer
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.Version
import net.minecraft.client.MinecraftClient
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
    }

    @JvmField var realTicks = 0
    override var status: Long = 0

    @Serializable(UUIDSerializer::class)
    var uuid: UUID? = null
    var serverId = ""
    var address: String = ""

    var worlds: MutableList<WorldData> = mutableListOf()

    val featureSet = mutableSetOf<String>()

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
