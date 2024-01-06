package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.IdentifierSerializer
import kotlinx.serialization.Serializable
import net.minecraft.client.MinecraftClient
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import kotlin.io.path.name

@Serializable
data class WorldInfo(
    val isRemoteServer: Boolean = false,
    val remoteServerHost: String? = null,
    val remoteServerPort: Int? = null,
    val remoteServerRedenVersion: String? = null,
    val remoteServerRedenVersionCode: Int? = null,
    val remoteServerRedenBrand: String? = null,
    val remoteServerWorldId: String? = null,
    @Serializable(with = IdentifierSerializer::class)
    val remoteServerWorldKey: Identifier? = null,
    @Serializable(with = IdentifierSerializer::class)
    val remoteServerWorldDimension: Identifier? = null,
    val localSaveName: String? = null,
    val localWorldId: String? = null,
    @Serializable(with = IdentifierSerializer::class)
    val localWorldKey: Identifier? = null,
    @Serializable(with = IdentifierSerializer::class)
    val localWorldDimension: Identifier? = null,
) {
    companion object {
        private fun ofRemote(mc: MinecraftClient): WorldInfo {
            val host = mc.currentServerEntry!!.address.substringBeforeLast(":")
            val port = mc.currentServerEntry!!.address.substringAfterLast(":").toIntOrNull() ?: 25565

            return WorldInfo(
                isRemoteServer = true,
                remoteServerHost = host,
                remoteServerPort = port,
                remoteServerRedenVersion = mc.serverData?.version?.friendlyString,
                remoteServerRedenVersionCode = -1,
                remoteServerRedenBrand = "TODO", // todo
                remoteServerWorldId = mc.world!!.data?.worldId,
                remoteServerWorldDimension = mc.world!!.dimensionKey.value,
                remoteServerWorldKey = mc.world!!.registryKey.value,
            )
        }

        private fun ofLocal(world: ServerWorld): WorldInfo {
            return WorldInfo(
                isRemoteServer = false,
                localSaveName = world.server.session.directory.path.name,
                localWorldId = world.data.worldId,
                localWorldDimension = world.dimensionKey.value,
                localWorldKey = world.registryKey.value,
            )
        }

        fun MinecraftClient.getWorldInfo() =
            if (server == null) ofRemote(this)
            else ofLocal(server!!.getWorld(world!!.registryKey)!!)
    }
}
