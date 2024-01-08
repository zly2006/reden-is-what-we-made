package com.github.zly2006.reden.rvc.tracking.reader

import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.tracking.IRvcFileReader
import com.github.zly2006.reden.rvc.tracking.RvcDataReader
import com.github.zly2006.reden.rvc.tracking.TrackPredicate
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.tick.TickPriority
import java.util.*

class RvcReaderV1(
    val header: IRvcFileReader.RvcHeader
): IRvcFileReader {
    override fun readBlocksData(data: List<String>): Map<BlockPos, BlockState> {
        val blocks = mutableMapOf<BlockPos, BlockState>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = BlockPos(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val blockState = NbtHelper.toBlockState(
                Registries.BLOCK.readOnlyWrapper,
                NbtHelper.fromNbtProviderString(rvcData.readGreedy())
            )
            blocks[blockPos] = blockState
        }
        return blocks
    }

    override fun readBlockEntitiesData(data: List<String>): Map<BlockPos, NbtCompound> {
        val blockEntities = mutableMapOf<BlockPos, NbtCompound>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = BlockPos(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val nbt = NbtHelper.fromNbtProviderString(rvcData.readGreedy())
            blockEntities[blockPos] = nbt
        }
        return blockEntities
    }

    override fun readEntitiesData(data: List<String>): Map<UUID, NbtCompound> {
        val entities = mutableMapOf<UUID, NbtCompound>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val uuid = UUID.fromString(rvcData.next())
            val nbt = NbtHelper.fromNbtProviderString(rvcData.readGreedy())
            entities[uuid] = nbt
        }
        return entities
    }

    override fun readTrackPointData(data: List<String>, structure: TrackedStructure): List<TrackedStructure.TrackPoint> {
        val trackPoints = mutableListOf<TrackedStructure.TrackPoint>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val predicate = rvcData.next()
            val mode = rvcData.next()
            trackPoints.add(
                TrackedStructure.TrackPoint(
                    pos = blockPos,
                    predicate = TrackPredicate.valueOf(predicate),
                    mode = TrackPredicate.TrackMode.valueOf(mode),
                    structure = structure
                )
            )
        }
        return trackPoints
    }

    override fun readBlockEventsData(data: List<String>): List<TrackedStructure.BlockEventInfo> {
        val blockEvents = mutableListOf<TrackedStructure.BlockEventInfo>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val relativeCoordinate = RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val type = rvcData.next().toInt()
            val data = rvcData.next().toInt()
            val block = Registries.BLOCK.get(Identifier(rvcData.readGreedy()))
            blockEvents.add(TrackedStructure.BlockEventInfo(
                pos = relativeCoordinate,
                type = type,
                data = data,
                block = block
            ))
        }
        return blockEvents
    }

    @Deprecated("Scheduled ticks data is not supported yet.")
    override fun readScheduledTicksData(data: List<String>): List<NbtCompound> {
        throw UnsupportedOperationException()
    }

    override fun <T> readScheduledTicksData(data: List<String>, registry: Registry<T>): List<TrackedStructure.TickInfo<T>> {
        val blockTicks = mutableListOf<TrackedStructure.TickInfo<T>>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val relativeCoordinate = RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            blockTicks.add(TrackedStructure.TickInfo(
                pos = relativeCoordinate,
                type = registry.get(Identifier(rvcData.next()))!! as T,
                delay = rvcData.next().toLong(),
                priority = TickPriority.byIndex(rvcData.next().toInt()),
                registry = registry
            ))
        }
        return blockTicks
    }
}
