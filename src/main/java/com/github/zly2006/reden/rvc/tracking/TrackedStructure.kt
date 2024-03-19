package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IWritableStructure
import com.github.zly2006.reden.rvc.RelativeCoordinate
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
    override val xSize: Int
        get() = TODO("Not yet implemented")
    override val ySize: Int
        get() = TODO("Not yet implemented")
    override val zSize: Int
        get() = TODO("Not yet implemented")

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

    fun refreshPositionsAsync() = networkWorker?.async { refreshPositions() }
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
    val minPos: BlockPos = TODO()
    override fun setBlockEntityData(pos: RelativeCoordinate, nbt: NbtCompound) {
        TODO("Not yet implemented")
    }

    override fun setBlockState(pos: RelativeCoordinate, state: BlockState) {
        TODO("Not yet implemented")
    }

    override fun getBlockEntityData(pos: RelativeCoordinate): NbtCompound? {
        TODO("Not yet implemented")
    }

    override fun getBlockState(pos: RelativeCoordinate): BlockState {
        TODO("Not yet implemented")
    }

    override fun getOrCreateBlockEntityData(pos: RelativeCoordinate): NbtCompound {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    fun getRelativeCoordinate(pos: BlockPos): RelativeCoordinate {
        TODO("Not yet implemented")
    }

    fun onBlockRemoved(pos: BlockPos) {

    }

    fun onBlockAdded(pos: BlockPos) {

    }
}
