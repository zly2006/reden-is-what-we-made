package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.RelativeCoordinate
import com.github.zly2006.reden.rvc.tracking.io.RvcFileIO
import com.github.zly2006.reden.rvc.tracking.network.NetworkWorker
import com.github.zly2006.reden.utils.redenError
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.nio.file.Path
import java.util.*

class TrackedStructure(
    override var name: String,
    val repository: RvcRepository?,
) : IWritableStructure, IPlacement {
    override val entities: MutableMap<UUID, NbtCompound>
        get() = regions.values.flatMap { it.entities.entries }.associate { it.toPair() }.toMutableMap()
    var networkWorker: NetworkWorker? = null
    override var enabled: Boolean = true
    override val structure get() = this
    val regions = mutableMapOf<String, TrackedStructurePart>()

    init {
        regions[""] = TrackedStructurePart("", this)
    }

    val minX get() = regions.values.minOf { it.minX }
    val minY get() = regions.values.minOf { it.minY }
    val minZ get() = regions.values.minOf { it.minZ }
    override val xSize get() = regions.values.maxOf { it.minX + it.xSize } - minX
    override val ySize get() = regions.values.maxOf { it.minY + it.ySize } - minY
    override val zSize get() = regions.values.maxOf { it.minZ + it.zSize } - minZ

    /**
     * This is stored in the file `.git/placement_info.json`.
     *
     * When we cloned or created a repository, remember to create this file.
     *
     * @see RvcRepository.placementInfo
     */
    var placementInfo: PlacementInfo? = null
    override val world: World
        get() = if (networkWorker?.world?.info == placementInfo?.worldInfo)
            networkWorker?.world ?: redenError("World is not set for $name")
        else placementInfo?.worldInfo?.getWorld() ?: redenError("World is not found: $name")
    override val origin: BlockPos
        get() = placementInfo?.origin?.toImmutable()
            ?: redenError("getting origin but PlacementInfo not set for $name")

    fun setPlaced() {
        require(repository != null) { "Repository is null" }
        repository.placed = true
        repository.placementInfo = this.placementInfo
    }

    override fun startMoving() {
        require(repository != null) { "Repository is null" }
        repository.placed = false
    }

    fun refreshPositionsAsync() = networkWorker?.launch { refreshPositions() }
    suspend fun refreshPositions() {
        regions.values.forEach {
            it.refreshPositions()
        }
    }

    /**
     * Remove this structure from the world, including all blocks
     */
    fun remove() {
        require(repository != null) { "Repository is null" }
        repository.placementInfo = null
        repository.placed = false
        clearArea()
    }

    suspend fun collectAllFromWorld() {
        regions.values.forEach {
            it.collectAllFromWorld()
        }
    }

    val totalBlocks: Int get() = regions.values.sumOf { it.blocks.size }
    val minPos get() = BlockPos(minX, minY, minZ)

    private fun getPartsFor(pos: RelativeCoordinate) =
        regions.values.filter {
            it.placementInfo != null &&
                    it.isInArea(
                        RelativeCoordinate(
                            pos.x + origin.x - it.origin.x,
                            pos.y + origin.y - it.origin.y,
                            pos.z + origin.z - it.origin.z
                        )
                    )
        }

    override fun setBlockEntityData(pos: RelativeCoordinate, nbt: NbtCompound) {
        val count = getPartsFor(pos).onEach { it.setBlockEntityData(pos, nbt) }.count()
        require(count > 0) { "No region contains $pos" }
    }

    override fun setBlockState(pos: RelativeCoordinate, state: BlockState) {
        val count = getPartsFor(pos).onEach { it.setBlockState(pos, state) }.count()
        require(count > 0) { "No region contains $pos" }
    }

    override fun getBlockEntityData(pos: RelativeCoordinate) =
        regions.values.firstNotNullOfOrNull { it.getBlockEntityData(pos) }

    override fun getBlockState(pos: RelativeCoordinate) =
        regions.values.firstNotNullOf {
            it.getBlockState(pos).takeIf { state -> !state.isAir }
        }

    override fun isInArea(pos: RelativeCoordinate) = regions.any { it.value.isInArea(pos) }
    override fun createPlacement(placementInfo: PlacementInfo) = apply {
        val oldOffsets = regions.mapValues { it.value.placementInfo?.origin?.subtract(this.origin) }
        this.placementInfo = placementInfo
        regions.forEach { (k, v) ->
            val partOrigin = oldOffsets[k]?.add(origin) ?: origin
            v.createPlacement(placementInfo.copy(origin = partOrigin))
        }
    }

    override fun save(path: Path) = RvcFileIO.save(path, this)
    override fun load(path: Path) = RvcFileIO.load(path, this)
    suspend fun autoTrack() {
        // todo
    }

    fun getRelativeCoordinate(pos: BlockPos): RelativeCoordinate {
        require(regions.isNotEmpty()) {
            "No region in this structure"
        }
        return RelativeCoordinate(pos.x - minX, pos.y - minY, pos.z - minZ)
    }

    fun onBlockRemoved(pos: BlockPos) {
        regions.values.forEach { it.onBlockRemoved(pos) }
    }

    fun onBlockAdded(pos: BlockPos) {
        regions.values.forEach { it.onBlockAdded(pos) }
    }
}
