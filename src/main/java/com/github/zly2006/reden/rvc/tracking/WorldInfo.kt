package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.access.ServerData.Companion.serverData
import com.github.zly2006.reden.access.WorldData.Companion.data
import com.github.zly2006.reden.debugger.breakpoint.BlockPosSerializer
import com.github.zly2006.reden.debugger.breakpoint.IdentifierSerializer
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.server
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.MinecraftClient
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*
import kotlin.io.path.name

@Serializable
data class WorldInfo(
    val isRemoteServer: Boolean = false,
    // remote server
    val remoteServerHost: String? = null,
    val remoteServerPort: Int? = null,
    val remoteServerRedenVersion: String? = null,
    val remoteServerRedenVersionCode: Int? = null,
    val remoteServerRedenBrand: String? = null,
    // local server
    val localSaveName: String? = null,
    // shared
    val worldId: String? = null,
    @Serializable(with = IdentifierSerializer::class)
    val worldKey: Identifier? = null,
    @Serializable(with = IdentifierSerializer::class)
    val worldDimension: Identifier? = null,
) {
    companion object {
        fun ofMemory(world: World) = WorldInfo(
            isRemoteServer = true,
            worldId = UUID.randomUUID().toString(),
            worldDimension = world.dimensionKey.value,
            worldKey = world.registryKey.value,
        ).apply {
            worldCache = world
        }

        private fun ofRemote(mc: MinecraftClient): WorldInfo? {
            if (mc.currentServerEntry == null) return null
            val host = mc.currentServerEntry!!.address.substringBeforeLast(":")
            val port = mc.currentServerEntry!!.address.substringAfterLast(":").toIntOrNull() ?: 25565

            return WorldInfo(
                isRemoteServer = true,
                remoteServerHost = host,
                remoteServerPort = port,
                remoteServerRedenVersion = mc.serverData?.version?.friendlyString,
                remoteServerRedenVersionCode = -1,
                remoteServerRedenBrand = "TODO", // todo
                worldId = mc.world!!.data?.worldId,
                worldDimension = mc.world!!.dimensionKey.value,
                worldKey = mc.world!!.registryKey.value,
            )
        }

        fun ofLocal(world: ServerWorld): WorldInfo {
            return WorldInfo(
                isRemoteServer = false,
                localSaveName = world.server.session.directory.path.name,
                worldId = world.data.worldId,
                worldDimension = world.dimensionKey.value,
                worldKey = world.registryKey.value,
            )
        }

        fun MinecraftClient.getWorldInfo() = of(world!!)

        fun of(world: World) =
            if (world is ServerWorld) ofLocal(world)
            else MinecraftClient.getInstance().server?.getWorld(world.registryKey)?.let { ofLocal(it) }
                ?: ofRemote(MinecraftClient.getInstance())
                ?: ofMemory(world)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is WorldInfo) return false
        if (other.worldId eqNotNull worldId) return true
        return if (isRemoteServer) {
            other.localSaveName eqNotNull localSaveName
                    && other.worldKey eqNotNull worldKey
        } else {
            other.remoteServerHost eqNotNull remoteServerHost
                    && other.remoteServerPort eqNotNull remoteServerPort
                    && other.worldKey eqNotNull worldKey
        }
    }

    override fun hashCode(): Int {
        if (worldId != null) return worldId.hashCode()
        return if (isRemoteServer) {
            (remoteServerHost.hashCode() * 31 + remoteServerPort.hashCode()) * 31 + worldKey.hashCode()
        } else {
            (localSaveName.hashCode() * 31 + worldKey.hashCode())
        }
    }

    @Transient
    @get:JvmName("world")
    @set:JvmName("world")
    var worldCache: World? = null

    fun getWorld(): World? {
        if (worldCache != null) return worldCache
        val registryKey = RegistryKey.of(RegistryKeys.WORLD, worldKey)
        worldCache = if (isClient) {
            val server = MinecraftClient.getInstance().server
            if (server != null) {
                server.getWorld(registryKey)
            }else
                MinecraftClient.getInstance().world
        } else {
            server.getWorld(registryKey)
        }
        return worldCache
    }

    fun getClientWorld(): World? {
        if (worldCache !is ServerWorld) return worldCache // allows: schematic world, client world
        val mc = MinecraftClient.getInstance()
        return mc.world?.takeIf { it.registryKey.value == worldKey }
    }
}

private infix fun <T> T?.eqNotNull(other: T?): Boolean {
    return this != null && other != null && this == other
}

val World.info: WorldInfo
    get() = WorldInfo.of(this)

@Serializable
data class PlacementInfo(
    val worldInfo: WorldInfo,
    @Serializable(with = BlockPosSerializer::class)
    val origin: BlockPos = BlockPos.ORIGIN,
    val isImported: Boolean = false,
    val isServerSide: Boolean = false,
)
