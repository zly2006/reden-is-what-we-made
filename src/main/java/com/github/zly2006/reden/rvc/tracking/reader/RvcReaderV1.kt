package com.github.zly2006.reden.rvc.tracking.reader

import com.github.zly2006.reden.rvc.tracking.IRvcFileReader
import com.github.zly2006.reden.rvc.tracking.RvcDataReader
import com.github.zly2006.reden.rvc.tracking.TrackPredicate
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.registry.Registries
import net.minecraft.server.world.BlockEvent
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*

class RvcReaderV1(
    header: IRvcFileReader.RvcHeader
): IRvcFileReader {
    override fun readBlocksData(data: List<String>): Map<BlockPos, BlockState> {
        val blocks = mutableMapOf<BlockPos, BlockState>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = BlockPos(rvcData.readNext().toInt(), rvcData.readNext().toInt(), rvcData.readNext().toInt())
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
            val blockPos = BlockPos(rvcData.readNext().toInt(), rvcData.readNext().toInt(), rvcData.readNext().toInt())
            val nbt = NbtHelper.fromNbtProviderString(rvcData.readGreedy())
            blockEntities[blockPos] = nbt
        }
        return blockEntities
    }

    override fun readEntitiesData(data: List<String>): Map<UUID, NbtCompound> {
        val entities = mutableMapOf<UUID, NbtCompound>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val uuid = UUID.fromString(rvcData.readNext())
            val nbt = NbtHelper.fromNbtProviderString(rvcData.readGreedy())
            entities[uuid] = nbt
        }
        return entities
    }

    override fun readTrackPointData(data: List<String>): List<TrackedStructure.TrackPoint> {
        val trackPoints = mutableListOf<TrackedStructure.TrackPoint>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = BlockPos(rvcData.readNext().toInt(), rvcData.readNext().toInt(), rvcData.readNext().toInt())
            val predicate = rvcData.readNext()
            val mode = rvcData.readNext()
            trackPoints.add(
                TrackedStructure.TrackPoint(
                    blockPos,
                    TrackPredicate.valueOf(predicate),
                    TrackPredicate.TrackMode.valueOf(mode),
                    null
                )
            )
        }
        return trackPoints
    }

    override fun readBlockEventsData(data: List<String>): List<BlockEvent> {
        val blockEvents = mutableListOf<BlockEvent>()
        data.forEach {
            val rvcData = RvcDataReader(it, ",")
            val blockPos = BlockPos(rvcData.readNext().toInt(), rvcData.readNext().toInt(), rvcData.readNext().toInt())
            val type = rvcData.readNext().toInt()
            val data = rvcData.readNext().toInt()
            val block = Registries.BLOCK.get(Identifier(rvcData.readGreedy()))
            blockEvents.add(BlockEvent(blockPos, block, type, data))
        }
        return blockEvents
    }

    override fun readScheduledTicksData(data: List<String>): List<NbtCompound> {
        val scheduledTicks = mutableListOf<NbtCompound>()
        data.forEach {
            scheduledTicks.add(NbtHelper.fromNbtProviderString(it))
        }
        return scheduledTicks
    }
}
