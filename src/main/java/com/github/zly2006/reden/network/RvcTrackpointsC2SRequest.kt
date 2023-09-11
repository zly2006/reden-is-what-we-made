package com.github.zly2006.reden.network

import com.github.zly2006.reden.rvc.tracking.RvcFileIO
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import com.github.zly2006.reden.utils.sendMessage
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path
import kotlin.io.path.writeBytes

class RvcTrackpointsC2SRequest(
    val trackpoints: List<TrackedStructure.TrackPoint>,
    val operation: Int,
    val name: String = "",
): FabricPacket {
    companion object {
        const val NOOP = 0
        const val REQUEST_DATA = 1
        const val SIGNED_COMMIT = 2

        val pType = PacketType.create(RVC_TRACKPOINTS_C2S) {
            val op = it.readVarInt()
            val size = it.readVarInt()
            val trackpoints = ArrayList<TrackedStructure.TrackPoint>(size)
            for (i in 0 until size) {
                trackpoints.add(
                    TrackedStructure.TrackPoint(
                        it.readBlockPos(),
                        TrackedStructure.TrackPredicate.valueOf(it.readString()),
                        TrackedStructure.TrackPoint.TrackMode.valueOf(it.readString()),
                    )
                )
            }
            val name = it.readString()
            RvcTrackpointsC2SRequest(trackpoints, op, name)
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                player.sendMessage("ss")
                fun sendStatus(status: Int) =
                    sender.sendPacket(RvcTrackpointsC2SRequest(listOf(), status))
                when (packet.operation) {
                    0 -> sendStatus(0)
                    1 -> {
                        val structure = TrackedStructure(packet.name)
                        structure.world = player.world
                        structure.trackPoints.addAll(packet.trackpoints)
                        structure.collectFromWorld()
                        val path = Path("rvc", "sync", player.entityName, packet.name)
                        RvcFileIO.save(path, structure)
                        val baStream = ByteArrayOutputStream()
                        val zipStream = ZipOutputStream(baStream)
                        zipStream.setComment("Reden Version Control")
                        path.toFile().listFiles()?.forEach {
                            zipStream.putNextEntry(ZipEntry(it.name))
                            it.inputStream().use { input ->
                                input.copyTo(zipStream)
                            }
                            zipStream.closeEntry()
                        }
                        path.parent.resolve(packet.name + ".zip").writeBytes(baStream.toByteArray())
                        sender.sendPacket(RvcDataS2CPacket(baStream.toByteArray()))
                        sendStatus(1)
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(operation)
        buf.writeVarInt(trackpoints.size)
        for (trackpoint in trackpoints) {
            buf.writeBlockPos(trackpoint.pos)
            buf.writeString(trackpoint.predicate.name)
            buf.writeString(trackpoint.mode.name)
        }
        buf.writeString(name)
    }

    override fun getType(): PacketType<*> = pType
}
