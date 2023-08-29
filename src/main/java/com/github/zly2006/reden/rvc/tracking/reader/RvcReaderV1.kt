package com.github.zly2006.reden.rvc.tracking.reader

import com.github.zly2006.reden.rvc.tracking.IRvcFileReader
import com.github.zly2006.reden.rvc.tracking.RvcDataReader
import com.github.zly2006.reden.rvc.tracking.RvcFileIO
import com.github.zly2006.reden.rvc.tracking.TrackedStructure
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.world.BlockEvent
import net.minecraft.util.math.BlockPos
import java.io.BufferedInputStream
import java.util.*

class RvcReaderV1(
    header: IRvcFileReader.RvcHeader
): IRvcFileReader {
    override fun readBlocksData(data: List<String>): Map<BlockPos, BlockState> {
        TODO("Not yet implemented")
//        readRvcFile(path, "blocks").forEach {
//            val data = RvcDataReader(it, ",")
//            val blockPos = BlockPos(data.readNext().toInt(), data.readNext().toInt(), data.readNext().toInt())
//            val blockState = NbtHelper.toBlockState(
//                Registries.BLOCK.readOnlyWrapper,
//                NbtHelper.fromNbtProviderString(data.readGreedy())
//            )
//            structure.blocks[blockPos] = blockState
//        }
    }

    override fun readBlockEntitiesData(data: List<String>): Map<BlockPos, NbtCompound> {
        TODO("Not yet implemented")
//        readRvcFile(path, "blockEntities").forEach {
//            val data = RvcDataReader(it, ",")
//            val blockPos = BlockPos(data.readNext().toInt(), data.readNext().toInt(), data.readNext().toInt())
//            val nbt = NbtHelper.fromNbtProviderString(data.readGreedy())
//            structure.blockEntities[blockPos] = nbt
//        }
    }

    override fun readEntitiesData(data: List<String>): Map<UUID, NbtCompound> {
        TODO("Not yet implemented")
//        readRvcFile(path, "entities").forEach {
//            val data = RvcDataReader(it, ",")
//            val uuid = UUID.fromString(data.readNext())
//            val nbt = NbtHelper.fromNbtProviderString(data.readGreedy())
//            structure.entities[uuid] = nbt
//        }

    }

    override fun readTrackPointData(data: List<String>): List<TrackedStructure.TrackPoint> {
        TODO("Not yet implemented")
//        structure.trackPoints.addAll(readRvcFile(path, "trackPoints").map {
//            val data = RvcDataReader(it, ",")
//            val blockPos = BlockPos(data.readNext().toInt(), data.readNext().toInt(), data.readNext().toInt())
//            val predicate = data.readNext()
//            val mode = data.readNext()
//            TrackedStructure.TrackPoint(
//                blockPos,
//                TrackedStructure.TrackPoint.TrackPredicate.valueOf(predicate),
//                TrackedStructure.TrackPoint.TrackMode.valueOf(mode)
//            )
//        })
    }

    override fun readBlockEventsData(data: List<String>): List<BlockEvent> {
        TODO("Not yet implemented")
//        readRvcFile(path, "blockEvents").forEach {
//            val split = it.split(",")
//            structure.blockEvents.add(
//                BlockEvent(
//                    BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt()),
//                    Registries.BLOCK.get(Identifier(split[5])),
//                    split[3].toInt(),
//                    split[4].toInt()
//                )
//            )
//        }
    }

    override fun readScheduledTicksData(data: List<String>): List<NbtCompound> {
        TODO("Not yet implemented")
//        readRvcFile(path, "blockScheduledTicks").forEach {
//            structure.xxxScheduledTicks.add(NbtHelper.fromNbtProviderString(it))
//        }
    }
}
