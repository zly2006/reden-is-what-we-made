package com.github.zly2006.reden.rvc.tracking

import net.minecraft.util.math.BlockPos
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.BlockEvent
import java.util.UUID

/**
 * Interface that defines the data reading methods for
 * reading RVC data and transform to [TrackedStructure] structures.
 */
interface IRvcFileReader {

    /**
     * Read blocks data from RVC files to maps for structure
     * @param data Lines contains data read form RVC files
     * @return [Map]<[BlockPos], [BlockState]> data that can be saved to a [TrackedStructure]
     */
    fun readBlocksData(data: List<String>): Map<BlockPos, BlockState>

    /**
     * Read blocks entities data from RVC files to maps for structure
     * @param data Lines contains data read form RVC files
     * @return [Map]<[BlockPos], [NbtCompound]> data that can be saved to a [TrackedStructure]
     */
    fun readBlockEntitiesData(data: List<String>): Map<BlockPos, NbtCompound>

    /**
     * Read entities data from RVC files to maps for structure
     * @param data Lines contains data read form RVC files
     * @return [Map]<[UUID], [NbtCompound]> data that can be saved to a [TrackedStructure]
     */
    fun readEntitiesData(data: List<String>): Map<UUID, NbtCompound>

    /**
     * Read track point data from RVC files to list for structure
     * @param data Lines contains data read form RVC files
     * @return [List]<[TrackedStructure.TrackPoint]> data that can be saved to a [TrackedStructure]
     */
    fun readTrackPointData(data: List<String>): List<TrackedStructure.TrackPoint>

    /**
     * Read block events data from RVC files to list for structure
     * @param data Lines contains data read form RVC files
     * @return [List]<[BlockEvent]> data that can be saved to a [TrackedStructure]
     */
    fun readBlockEventsData(data: List<String>): List<BlockEvent>

    /**
     * Read scheduled ticks data from RVC files to list for structure.
     * (This is used for both block scheduled ticks and fluid scheduled ticks.)
     * @param data Lines contains data read form RVC files
     * @return [List]<[NbtCompound]> data that can be saved to a [TrackedStructure]
     */
    fun readScheduledTicksData(data: List<String>): List<NbtCompound>

    /**
     * RVC File with a [IRvcFileReader] (for the corresponding data version)
     * and data [List] of [String]s.
     */
    data class RvcFile(
        val reader: IRvcFileReader,
        val data: List<String>
    )

    /**
     * RVC File metadata header
     */
    data class RvcHeader(
        val metadata: MutableMap<String, String> = mutableMapOf()
    ) {
        /**
         * @param header The string containing the header data from the RVC file.
         */
        constructor(header: String): this() {
            if (!header.startsWith("RVC; ")) {
                throw IllegalArgumentException("Invalid RVC header")
            }
            header.split("; ").drop(1).forEach {
                val key = it.substringBefore(": ")
                val value = it.substringAfter(": ")
                metadata[key] = value
            }
        }

        /**
         * @return Generated RVC data string for RVC data files.
         */
        override fun toString(): String {
            var str = "RVC; "
            metadata.forEach { (key, value) ->
                str += "$key: $value; "
            }
            return str.substring(0, str.length - 2)
        }
    }
}