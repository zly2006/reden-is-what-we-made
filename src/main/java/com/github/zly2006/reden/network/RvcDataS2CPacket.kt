package com.github.zly2006.reden.network

import com.github.zly2006.reden.utils.isClient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.network.PacketByteBuf
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.ByteArrayOutputStream

class RvcDataS2CPacket(
    val data: ByteArray
): FabricPacket {
    companion object {
        val pType = PacketType.create(RVC_DATA_SYNC) {
            val length = it.readLong()
            val data = ByteArray(length.toInt())
            val decompressed = GzipCompressorInputStream(data.inputStream()).readAllBytes()
            RvcDataS2CPacket(decompressed)
        }!!
        internal var consumer: ((ByteArray) -> Unit)? = null

        fun register() {
            if (isClient) {
                ClientPlayNetworking.registerGlobalReceiver(pType) { packet, player, sender ->
                    consumer?.invoke(packet.data)
                    consumer = null
                }
            }
        }
    }

    override fun write(buf: PacketByteBuf) {
        val stream = ByteArrayOutputStream()
        GzipCompressorOutputStream(stream).use { it.write(data) }
        val compressedData = stream.toByteArray()
        buf.writeLong(compressedData.size.toLong())
        buf.writeBytes(compressedData)
    }

    override fun getType(): PacketType<*> = pType
}
