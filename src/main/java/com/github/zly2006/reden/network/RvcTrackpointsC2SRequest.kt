package com.github.zly2006.reden.network

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.tracking.RvcFileIO
import com.github.zly2006.reden.rvc.tracking.TrackPredicate
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
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
    val operation: Int,
    val structure: TrackedStructure
): FabricPacket {
    companion object {
        const val NOOP = 0
        const val REQUEST_DATA = 1
        const val SIGNED_COMMIT = 2

        val id = Reden.identifier("rvc_trackpoints_c2s")
        val pType = PacketType.create(id) {
            val op = it.readVarInt()
            val structure = TrackedStructure(it.readString())
            val size = it.readVarInt()
            val trackpoints = ArrayList<TrackedStructure.TrackPoint>(size)
            for (i in 0 until size) {
                trackpoints.add(
                    TrackedStructure.TrackPoint(
                        structure.getRelativeCoordinate(it.readBlockPos()),
                        TrackPredicate.valueOf(it.readString()),
                        TrackPredicate.TrackMode.valueOf(it.readString()),
                        structure
                    )
                )
            }
            RvcTrackpointsC2SRequest(op, structure)
        }!!

        fun register() {
            ServerPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                fun sendStatus(status: Int) =
                    sender.sendPacket(RvcTrackpointsC2SRequest(status, packet.structure))
                when (packet.operation) {
                    0 -> sendStatus(0)
                    1 -> {
                        packet.structure.world = player.world
                        packet.structure.trackPoints.addAll(packet.structure.trackPoints)
                        packet.structure.collectFromWorld()
                        val path = Path("rvc", "sync", player.nameForScoreboard, packet.structure.name)
                        RvcFileIO.save(path, packet.structure)
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
                        path.parent.resolve("${packet.structure.name}.zip").writeBytes(baStream.toByteArray())
                        sender.sendPacket(RvcDataS2CPacket(baStream.toByteArray()))
                        sendStatus(1)
                    }
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(operation)
        buf.writeString(structure.name)
        buf.writeVarInt(structure.trackPoints.size)
        for (trackpoint in structure.trackPoints) {
            buf.writeBlockPos(trackpoint.pos)
            buf.writeString(trackpoint.predicate.name)
            buf.writeString(trackpoint.mode.name)
        }
    }

    override fun getType(): PacketType<*> = pType
}
