package com.github.zly2006.reden.rvc.tracking

import io.netty.buffer.Unpooled
import net.minecraft.network.PacketByteBuf
import java.nio.file.Path

@Deprecated("Deprecated because a new tracking & versioning system is being developed")
class FileSystemStorage(
    private val root: Path
): TrackedDiffStorage {
    override fun get(id: Long): TrackedDiff? {
        val path = String.format(".rvc/objects/%02x/%014x", id and 0xff, id and (-1L xor 0xffL))
        val file = root.resolve(path).toFile()
        if (file.exists()) {
            return PacketBufDiffSerializer.readPacketBuf(PacketByteBuf(Unpooled.wrappedBuffer(file.readBytes())))
        }
        return null
    }

    override fun store(trackedDiff: TrackedDiff): Long {
        TODO("Not yet implemented")
    }

    override fun getRef(tag: String): Long {
        TODO("Not yet implemented")
    }

    override fun addRef(tag: String, id: Long): Boolean {
        TODO("Not yet implemented")
    }
}
