package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.Person
import com.github.zly2006.reden.rvc.nbt.DummyDiff
import com.github.zly2006.reden.rvc.nbt.NbtDiff
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtHelper
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.util.math.BlockPos
import java.util.*

@Deprecated("Deprecated because a new tracking & versioning system is being developed")
object PacketBufDiffSerializer {
    fun toPacketBuf(buf: PacketByteBuf, diff: TrackedDiff) {
        buf.writeLongArray(diff.parentIds)
        diff.originDiff.forEach(buf::writeBlockPos)
        buf.writeVarInt(diff.xSize)
        buf.writeVarInt(diff.ySize)
        buf.writeVarInt(diff.zSize)
        buf.writeString(diff.message)
        buf.writeLong(diff.timestamp)
        buf.writeVarInt(diff.removedBlockPoses.size)
        diff.removedBlockPoses.forEach(buf::writeBlockPos)
        buf.writeVarInt(diff.changedBlocks.size)
        val palette = mutableMapOf<BlockState, Int>()
        val paletteList = mutableListOf<BlockState>()
        val blockPosIntSortedMap = diff.changedBlocks.map {
            it.key.asLong() to palette.getOrPut(it.value) {
                paletteList.add(it.value)
                paletteList.size - 1
            }
        }.toSortedMap()
        buf.writeVarInt(paletteList.size)
        paletteList.forEach {
            buf.writeNbt(NbtHelper.fromBlockState(it))
        }
        buf.writeVarInt(blockPosIntSortedMap.size)
        blockPosIntSortedMap.forEach {
            buf.writeLong(it.key)
            buf.writeVarInt(it.value)
        }
        buf.writeVarInt(diff.changedBlockEntities.size)
        diff.changedBlockEntities.forEach {
            buf.writeBlockPos(it.key)
            it.value.writeBuf(buf)
        }
        buf.writeVarInt(diff.entities.size)
        diff.entities.forEach {
            buf.writeUuid(it.key)
            it.value.writeBuf(buf)
        }
    }

    fun readPacketBuf(buf: PacketByteBuf): TrackedDiff {
        val parentIds = buf.readLongArray()
        val originDiff = Array(parentIds.size) { buf.readBlockPos() }
        val xSize = buf.readVarInt()
        val ySize = buf.readVarInt()
        val zSize = buf.readVarInt()
        val message = buf.readString()
        val timestamp = buf.readLong()
        val removedBlockPoses = mutableSetOf<BlockPos>()
        repeat(buf.readVarInt()) {
            removedBlockPoses.add(buf.readBlockPos())
        }
        val paletteSize = buf.readVarInt()
        val paletteList = ArrayList<BlockState>(paletteSize)
        repeat(paletteSize) {
            paletteList.add(NbtHelper.toBlockState(Registries.BLOCK.readOnlyWrapper, buf.readNbt()))
        }
        val changedBlocks = sortedMapOf<BlockPos, BlockState>()
        repeat(buf.readVarInt()) {
            val pos = buf.readLong()
            val state = paletteList[buf.readVarInt()]
            changedBlocks[BlockPos.fromLong(pos)] = state
        }
        val changedBlockEntities = sortedMapOf<BlockPos, NbtDiff>()
        repeat(buf.readVarInt()) {
            val pos = buf.readBlockPos()
            changedBlockEntities[pos] = DummyDiff // FIXME:
        }
        val entities = mutableMapOf<UUID, NbtDiff>()
        repeat(buf.readVarInt()) {
            val uuid = buf.readUuid()
            entities[uuid] = DummyDiff // FIXME:
        }
        val diff = TrackedDiff(
            parentIds,
            originDiff,
            xSize,
            ySize,
            zSize,
            changedBlocks,
            changedBlockEntities,
            removedBlockPoses,
            entities,
            message,
            timestamp,
            Person("unknown", "unknown", null, "un")
        )
        return diff
    }

    fun toByteArray(diff: TrackedDiff): ByteArray? {
        Unpooled.wrappedBuffer(ByteArray(0))
        val buf = PacketByteBufs.create()
        toPacketBuf(buf, diff)
        return buf.array()
    }
}

private fun <K : Comparable<K>, V> Iterable<Pair<K, V>>.toSortedMap(): SortedMap<K, V> {
    val map = sortedMapOf<K, V>()
    for ((k, v) in this) {
        map[k] = v
    }
    return map
}
