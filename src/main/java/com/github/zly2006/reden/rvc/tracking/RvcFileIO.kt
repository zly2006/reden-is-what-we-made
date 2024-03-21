package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.io.Palette
import com.github.zly2006.reden.rvc.io.StructureIO
import com.github.zly2006.reden.rvc.tracking.reader.RvcReaderV1
import com.github.zly2006.reden.utils.Utils
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.visitor.StringNbtWriter
import net.minecraft.registry.Registries
import java.io.FileFilter
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.notExists

/**
 * Save and load [TrackedStructure]s into and from RVC files.
 */
object RvcFileIO : StructureIO {
    const val CURRENT_VERSION = "1.0.0"
    private val RVC_HEADER = IRvcFileReader.RvcHeader(
        mutableMapOf(
            "Version" to CURRENT_VERSION,
            "Platform" to "MCMod/Reden"
        )
    )

    /**
     * Map of data version number to [IRvcFileReader] for reading data from RVC files.
     */
    private val VERSION_TO_READER = mapOf<String, (IRvcFileReader.RvcHeader) -> IRvcFileReader>(
        "1.0.0" to { RvcReaderV1(it) }
    )

    /**
     * Write to a RVC file.
     * @param path Path to the RVC file to write to
     * @param name Name of the RVC file to write to
     * @param header The header of the RVC file containing the metadata
     * @param data The data to write to the RVC file
     */
    private fun writeRvcFile(path: Path, name: String, header: IRvcFileReader.RvcHeader, data: String) {
        path.resolve("$name.rvc").toFile().writeText("$header\n$data")
    }

    /**
     * Get the [IRvcFileReader] for the RVC file with its data version number.
     * @param header The header of the RVC file containing the data version number
     * @return [IRvcFileReader] for the RVC file with its data version number
     */
    private fun getRvcFileReader(header: String): IRvcFileReader {
        val header = IRvcFileReader.RvcHeader(header)
        val version = header.metadata["Version"] ?: CURRENT_VERSION
        val reader = VERSION_TO_READER[version] ?: throw IllegalArgumentException("Invalid RVC version")
        return reader(header)
    }

    /**
     * Load the RVC file to read data from.
     * @param path Path to the RVC file to load from
     * @param name Name of the RVC file to load from
     * @return [IRvcFileReader.RvcFile] that contains the correct [IRvcFileReader]
     * for its data version and the data to read
     */
    fun loadRvcFile(path: Path, name: String): IRvcFileReader.RvcFile? {
        val filename = "$name.rvc"
        if (path.resolve(filename).toFile().exists()) {
            val lines = path.resolve(filename).toFile().readLines()
            val rvcReader = getRvcFileReader(lines[0])
            val data = lines.subList(1, lines.size)
            return IRvcFileReader.RvcFile(rvcReader, data)
        }
        return null
    }

    /**
     * Save a [TrackedStructure] to a RVC file.
     * @param path Path to the RVC file to save to
     * @param structure The [TrackedStructure] to save data from
     * @throws IllegalArgumentException if the structure is not a [TrackedStructure]
     */
    override fun save(path: Path, structure: IStructure) {
        save(path, structure, null)
    }

    fun save(path: Path, structure: IStructure, palette: Boolean?) {
        // ================================ Check Saving Structure Type ================================
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }
        if (path.notExists()) {
            path.toFile().mkdirs()
        }
        structure.regions.values.forEach { part ->
            val path = path.resolve(part.name)
            val usePalette = palette ?: (part.blocks.size > 1000)

            @Suppress("NAME_SHADOWING")
            val palette = Palette()

            // ======================================== Save Blocks ========================================
            // public final val blocks: MutableMap<BlockPos, BlockState>
            // com.github.zly2006.reden.rvc.ReadWriteStructure
            part.blocks.entries.joinToString("\n") { (pos, state) ->
                if (usePalette)
                    "${pos.x},${pos.y},${pos.z},${palette.getId(toNbtString(state))}"
                else
                    "${pos.x},${pos.y},${pos.z},${toNbtString(state)}"
            }.let { data ->
                writeRvcFile(
                    path, "blocks", IRvcFileReader.RvcHeader(
                        mutableMapOf(
                            "Version" to CURRENT_VERSION,
                            "Platform" to "MCMod/Reden",
                            "Palette" to usePalette.toString()
                        )
                    ), data
                )
            }

            // ==================================== Save Block Entities ====================================
            // public final val blockEntities: MutableMap<BlockPos, NbtCompound>
            // com.github.zly2006.reden.rvc.ReadWriteStructure
            part.blockEntities.entries.joinToString("\n") { (pos, nbt) ->
                if (usePalette)
                    "${pos.x},${pos.y},${pos.z},${palette.getId(toNbtString(nbt))}"
                else
                    "${pos.x},${pos.y},${pos.z},${toNbtString(nbt)}"
            }.let { data ->
                writeRvcFile(
                    path, "blockEntities", IRvcFileReader.RvcHeader(
                        mutableMapOf(
                            "Version" to CURRENT_VERSION,
                            "Platform" to "MCMod/Reden",
                            "Palette" to usePalette.toString()
                        )
                    ), data
                )
            }

            // ======================================= Save Entities =======================================
            // public open val entities: MutableMap<UUID, NbtCompound>
            // com.github.zly2006.reden.rvc.ReadWriteStructure
            part.entities.entries.joinToString("\n") { (uuid, nbt) ->
                "$uuid,${toNbtString(nbt)}"
            }.let { data -> writeRvcFile(path, "entities", RVC_HEADER, data) }

            // ===================================== Save Track Points =====================================
            // public final val trackPoints: MutableList<TrackedStructure.TrackPoint>
            // com.github.zly2006.reden.rvc.tracking.TrackedStructure
            // todo
            part.tracker
//            part.trackPoints.joinToString("\n") { trackPoint ->
//                val coordinate = part.getRelativeCoordinate(trackPoint.pos)
//                "${coordinate.x},${coordinate.y},${coordinate.z},${trackPoint.predicate},${trackPoint.mode}"
//            }.let { data -> writeRvcFile(path, "trackPoints", RVC_HEADER, data) }

            // ===================================== Save Block Events =====================================
            // public final val blockEvents: MutableList<BlockEvent>
            // com.github.zly2006.reden.rvc.tracking.TrackedStructure
            part.blockEvents.joinToString("\n") { blockEvent ->
                blockEvent.toRvcDataString()
            }.let { data -> writeRvcFile(path, "blockEvents", RVC_HEADER, data) }

            // ================================ Save Block Scheduled Ticks =================================
            part.blockScheduledTicks.joinToString(
                "\n",
                transform = TrackedStructurePart.TickInfo<*>::toRvcDataString
            )
                .let { data -> writeRvcFile(path, "blockScheduledTicks", RVC_HEADER, data) }

            // ================================ Save Fluid Scheduled Ticks =================================
            part.fluidScheduledTicks.joinToString(
                "\n",
                transform = TrackedStructurePart.TickInfo<*>::toRvcDataString
            )
                .let { data -> writeRvcFile(path, "fluidScheduledTicks", RVC_HEADER, data) }

            if (usePalette) {
                palette.idToName.entries.joinToString("\n") { (id, name) ->
                    "$id,$name"
                }.let { data -> writeRvcFile(path, "palette", RVC_HEADER, data) }
            }
        }
    }

    private fun toNbtString(nbt: NbtCompound) =
        StringNbtWriter().apply(nbt)

    private fun toNbtString(state: BlockState) =
        Registries.BLOCK.getId(state.block).toString() + state.properties.map {
            it to state[it]
        }.joinToString(",", "[", "]") {
            "${it.first.name}=${Utils.blockPropertyName(it.first, it.second)}"
        }

    /**
     * Load a [TrackedStructure] from a RVC file.
     * @param path Path to the RVC file to load from
     * @param structure The [TrackedStructure] to load data into
     * @throws IllegalArgumentException if the structure is not a [TrackedStructure]
     */
    /*
     * Note from @Cubik65536: This kind of design is not necessary for now as we only have one version of data,
     *                        but it is good as it will allow us to read even from different versions of data
     *                        at the same time in the future.
     *                        If you have any suggestions on how to improve this or have a new design,
     *                        please let me know or make a pull request.
     */
    override fun load(path: Path, structure: IWritableStructure) {
        if (structure !is TrackedStructure) {
            throw IllegalArgumentException("Structure is not a TrackedStructure")
        }
        structure.regions.clear()
        val paths = path.toFile().listFiles(FileFilter { it.isDirectory && it.resolve("index.rvc").exists() })?.map {
            it.toPath()
        }.orEmpty()
        (paths + path).forEach {
            val part = TrackedStructurePart(it.name, structure)
            structure.regions[it.name] = part
            part.dirty = true // mark it as dirty caz we have no cache of positions
            val palette = Palette.load(loadRvcFile(path, "palette"))
            loadRvcFile(path, "blocks")?.let { rvcFile ->
                part.blocks.putAll(rvcFile.reader.readBlocksData(rvcFile.data, palette))
            }
            loadRvcFile(path, "blockEntities")?.let { rvcFile ->
                part.blockEntities.putAll(rvcFile.reader.readBlockEntitiesData(rvcFile.data, palette))
            }
            loadRvcFile(path, "entities")?.let { rvcFile ->
                part.entities.putAll(rvcFile.reader.readEntitiesData(rvcFile.data))
            }
            loadRvcFile(path, "trackPoints")?.let { rvcFile ->
//                part.trackPoints.addAll(rvcFile.reader.readTrackPointData(rvcFile.data))
                // todo
                part.tracker // .load()
            }
            loadRvcFile(path, "blockEvents")?.let { rvcFile ->
                part.blockEvents.addAll(rvcFile.reader.readBlockEventsData(rvcFile.data))
            }
            loadRvcFile(path, "blockScheduledTicks")?.let { rvcFile ->
                part.blockScheduledTicks.addAll(
                    rvcFile.reader.readScheduledTicksData(
                        rvcFile.data,
                        Registries.BLOCK
                    )
                )
            }
            loadRvcFile(path, "fluidScheduledTicks")?.let { rvcFile ->
                part.fluidScheduledTicks.addAll(
                    rvcFile.reader.readScheduledTicksData(
                        rvcFile.data,
                        Registries.FLUID
                    )
                )
            }
        }
    }
}
