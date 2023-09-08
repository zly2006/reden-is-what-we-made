package com.github.zly2006.reden.network

import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf

class RvcTrackpointsC2SRequest(
    val trackpoints: List<TrackedStructure.TrackPoint>,
    val operation: Int
): FabricPacket {
    companion object {
        const val NOOP = 0
        const val REQUEST_DATA = 1
        const val SIGNED_COMMIT = 2

        val pType = PacketType.create(RVC_TRACKPOINTS_C2S) {
            val size = it.readVarInt()
            val op = it.readVarInt()
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
            RvcTrackpointsC2SRequest(trackpoints, op)
        }!!
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(trackpoints.size)
        buf.writeVarInt(operation)
        for (trackpoint in trackpoints) {
            buf.writeBlockPos(trackpoint.pos)
            buf.writeString(trackpoint.predicate.name)
            buf.writeString(trackpoint.mode.name)
        }
    }

    override fun getType(): PacketType<*> = pType
}
