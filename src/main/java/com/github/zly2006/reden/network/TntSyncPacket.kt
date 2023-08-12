package com.github.zly2006.reden.network

import com.github.zly2006.reden.malilib.DEBUG_LOGGER
import com.github.zly2006.reden.pearl.pearlTask
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.sendMessage
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d
import java.util.*

private val pType = PacketType.create(TNT_SYNC_PACKET) {
    val projectileUUID = it.readUuid()
    val projectilePos = Vec3d(it.readDouble(), it.readDouble(), it.readDouble())
    val projectileMotion = Vec3d(it.readDouble(), it.readDouble(), it.readDouble())
    val tntPower = it.readFloat()
    val tntPos = Vec3d(it.readDouble(), it.readDouble(), it.readDouble())
    TntSyncPacket(projectileUUID, projectilePos, projectileMotion, tntPower, tntPos)
}

class TntSyncPacket(
    val projectileUUID: UUID,
    val projectilePos: Vec3d,
    val projectileMotion: Vec3d,
    val tntPower: Float,
    val tntPos: Vec3d
): FabricPacket {
    override fun write(buf: PacketByteBuf) {
        buf.writeUuid(projectileUUID)
        buf.writeDouble(projectilePos.x)
        buf.writeDouble(projectilePos.y)
        buf.writeDouble(projectilePos.z)
        buf.writeDouble(projectileMotion.x)
        buf.writeDouble(projectileMotion.y)
        buf.writeDouble(projectileMotion.z)
        buf.writeFloat(tntPower)
        buf.writeDouble(tntPos.x)
        buf.writeDouble(tntPos.y)
        buf.writeDouble(tntPos.z)
    }

    override fun getType(): PacketType<*> = pType

    companion object {
        val syncedTntPos = mutableSetOf<Vec3d>()
        fun register() {
            ServerTickEvents.END_SERVER_TICK.register {
                syncedTntPos.clear()
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, client, _ ->
                    pearlTask?.onTntSyncPacket(packet)
                    if (DEBUG_LOGGER.booleanValue) {
                        client.sendMessage("TntSyncPacket: TNT${packet.tntPower} @ ${packet.tntPos}")
                    }
                }
            }
        }
    }
}