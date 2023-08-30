package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.utils.longFromByteArray
import com.github.zly2006.reden.utils.toByteArray
import io.netty.buffer.Unpooled
import net.minecraft.network.PacketByteBuf
import java.io.IOException
import java.nio.file.Path

class FileSystemStorage(
    private val root: Path
): TrackedDiffStorage {

    private fun generatePathFromId(id: Long): String = String.format(".rvc/objects/%02x/%014x", id and 0xff, id and (-1L xor 0xffL))

    override fun get(id: Long): TrackedDiff? {
        val path = generatePathFromId(id)
        val file = root.resolve(path).toFile()
        if (file.exists()) {
            return PacketBufDiffSerializer.readPacketBuf(PacketByteBuf(Unpooled.wrappedBuffer(file.readBytes())))
        }
        return null
    }

    private fun writeToId(id: Long, trackedDiff: TrackedDiff) {
        val file = root.resolve(generatePathFromId(id)).toFile()
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeBytes(PacketBufDiffSerializer.toByteArray(trackedDiff) ?: throw IOException("Failed to serialize tracked diff"))
    }

    // Mark as synchronized to prevent from data race.
    @Synchronized
    override fun store(trackedDiff: TrackedDiff): Long {
        val file = root.resolve(".rvc/objects/__id__").toFile()
        var lastId: Long? = null
        if (!file.exists()) {
            file.createNewFile()
            file.writeBytes((0L).toByteArray())
            lastId = 0
        }
        val nextId =
            lastId ?: (longFromByteArray(file.readBytes())?.plus(1)) ?: throw IOException("Failed to generate next ID")
        file.writeBytes(nextId.toByteArray())
        writeToId(nextId, trackedDiff)
        return nextId
    }

    override fun getRef(tag: String): Long {
        TODO("Not yet implemented")
    }

    override fun addRef(tag: String, id: Long): Boolean {
        TODO("Not yet implemented")
    }
}