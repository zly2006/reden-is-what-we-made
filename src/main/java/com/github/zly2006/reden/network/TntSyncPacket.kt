package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.pearl.pearlTask
import com.github.zly2006.reden.utils.codec.UUIDSerializer
import com.github.zly2006.reden.utils.codec.Vec3dSerializer
import com.github.zly2006.reden.utils.debugLogger
import com.github.zly2006.reden.utils.isClient
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.Vec3d
import java.util.*

@Serializable
class TntSyncPacket(
    @Serializable(UUIDSerializer::class)
    val projectileUUID: UUID,
    @Serializable(Vec3dSerializer::class)
    val projectilePos: Vec3d,
    @Serializable(Vec3dSerializer::class)
    val projectileMotion: Vec3d,
    val tntPower: Float,
    @Serializable(Vec3dSerializer::class)
    val tntPos: Vec3d
) : CustomPayload {
    override fun getId() = ID

    companion object : PacketCodecHelper<TntSyncPacket> by PacketCodec(Reden.identifier("tnt_sync_packet")) {
        val syncedTntPos = mutableSetOf<Vec3d>()
        fun register() {
            ServerTickEvents.END_SERVER_TICK.register {
                syncedTntPos.clear()
            }
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(ID) { packet, _ ->
                    pearlTask?.onTntSyncPacket(packet)
                    debugLogger("TntSyncPacket: TNT${packet.tntPower} @ ${packet.tntPos}")
                }
            }
        }
    }
}
