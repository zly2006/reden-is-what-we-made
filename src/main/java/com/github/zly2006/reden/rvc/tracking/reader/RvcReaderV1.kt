package com.github.zly2006.reden.rvc.tracking.reader

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.io.Palette
import com.github.zly2006.reden.rvc.tracking.*
import net.minecraft.block.BlockState
import net.minecraft.command.argument.BlockArgumentParser
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.world.tick.TickPriority
import java.util.*

class RvcReaderV1(
    override val header: IRvcFileReader.RvcHeader
) : IRvcFileReader {
    override fun readBlocksData(data: List<String>, palette: Palette): Map<RelativeCoordinate, BlockState> {
        val usePalette = header.metadata["Palette"]?.toBoolean() ?: false
        val blocks = mutableMapOf<RelativeCoordinate, BlockState>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val blockData = if (usePalette) palette.getName(rvcData.next().toInt()) else rvcData.readGreedy()

            try {
                blocks[blockPos] =
                    BlockArgumentParser.block(Registries.BLOCK.readOnlyWrapper, blockData, false).blockState
            } catch (e: Exception) {
                Reden.LOGGER.error("Failed to load block state at $blockPos, $blockData", e)
            }
        }
        return blocks
    }

    override fun readBlockEntitiesData(data: List<String>, palette: Palette): Map<RelativeCoordinate, NbtCompound> {
        val usePalette = header.metadata["Palette"]?.toBoolean() ?: false
        val blockEntities = mutableMapOf<RelativeCoordinate, NbtCompound>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val nbt = NbtHelper.fromNbtProviderString(
                if (usePalette) palette.getName(rvcData.next().toInt()) else rvcData.readGreedy()
            )
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

    @Deprecated("#101")
    fun readTrackPointData(
        data: List<String>
    ): List<TrackPoint> {
        val trackPoints = mutableListOf<TrackPoint>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val predicate = rvcData.next()
            val mode = rvcData.next()
            trackPoints.add(
                TrackPoint(
                    relativeCoordinate = blockPos,
                    predicate = TrackPredicate.valueOf(predicate),
                    mode = TrackPredicate.TrackMode.valueOf(mode)
                )
            )
        }
        return trackPoints
    }

    override fun readBlockEventsData(data: List<String>): List<TrackedStructurePart.BlockEventInfo> {
        val blockEvents = mutableListOf<TrackedStructurePart.BlockEventInfo>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val relativeCoordinate =
                RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            val type = rvcData.next().toInt()
            val data = rvcData.next().toInt()
            val block = Registries.BLOCK.get(Identifier(rvcData.readGreedy()))
            blockEvents.add(
                TrackedStructurePart.BlockEventInfo(
                    pos = relativeCoordinate,
                    type = type,
                    data = data,
                    block = block
                )
            )
        }
        return blockEvents
    }

    override fun <T> readScheduledTicksData(
        data: List<String>,
        registry: Registry<T>
    ): List<TrackedStructurePart.TickInfo<T>> {
        val blockTicks = mutableListOf<TrackedStructurePart.TickInfo<T>>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val relativeCoordinate =
                RelativeCoordinate(rvcData.next().toInt(), rvcData.next().toInt(), rvcData.next().toInt())
            blockTicks.add(
                TrackedStructurePart.TickInfo(
                    pos = relativeCoordinate,
                    type = registry.get(Identifier(rvcData.next()))!! as T,
                    delay = rvcData.next().toLong(),
                    priority = TickPriority.byIndex(rvcData.next().toInt()),
                    registry = registry
                )
            )
        }
        return blockTicks
    }
}
