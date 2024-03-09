package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.*
import com.github.zly2006.reden.rvc.tracking.network.NetworkWorker
import com.github.zly2006.reden.utils.redenError
import com.github.zly2006.reden.utils.setBlockNoPP
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.*
import net.minecraft.world.World
import net.minecraft.world.tick.ChunkTickScheduler
import net.minecraft.world.tick.OrderedTick
import net.minecraft.world.tick.TickPriority
import org.jetbrains.annotations.Contract
import java.nio.file.Path
import java.util.*

/**
 * todo: see #81
 */
class TrackedStructure(
    name: String,
    val repository: RvcRepository?,
) : ReadWriteStructure(name), IPlacement, PositionIterable {
    lateinit var networkWorker: NetworkWorker
    override var enabled: Boolean = true
    override val structure = this

    /**
     * This is stored in the file `.git/placement_info.json`.
     *
     * When we cloned or created a repository, remember to create this file.
     *
     * @see RvcRepository.placementInfo
     */
    var placementInfo: PlacementInfo? = null
    override val world: World
        get() = networkWorker?.world
            ?: redenError("getting world but networkWorker not set for $name")

    override val origin: BlockPos
        get() = placementInfo?.origin?.toImmutable()
            ?: redenError("getting origin but PlacementInfo not set for $name")

    override fun createPlacement(world: World, origin: BlockPos) = apply {
        placementInfo = PlacementInfo(WorldInfo.of(world), origin)
    }

    var cachedPositions = HashMap<BlockPos, TrackPoint>()
    var cachedIgnoredPositions = HashMap<BlockPos, TrackPoint>()
    val trackPoints = mutableListOf<TrackPoint>()
    val blockEvents = mutableListOf<BlockEventInfo>() // order sensitive
    val blockScheduledTicks = mutableListOf<TickInfo<Block>>() // order sensitive
    val fluidScheduledTicks = mutableListOf<TickInfo<Fluid>>() // order sensitive
    var dirty = true

    data class TickInfo<T>(
        val pos: RelativeCoordinate,
        val type: T,
        val delay: Long,
        val priority: TickPriority,
        val registry: Registry<T>
    ) {
        fun toRvcDataString(): String {
            return "${pos.x},${pos.y},${pos.z},${registry.getId(type)},$delay,${priority.ordinal}"
        }

        companion object {
            fun <T> wrap(orderedTick: OrderedTick<T>, world: World): TickInfo<T> {
                @Suppress("UNCHECKED_CAST")
                return TickInfo(
                    pos = RelativeCoordinate(orderedTick.pos.x, orderedTick.pos.y, orderedTick.pos.z),
                    type = orderedTick.type as T,
                    delay = orderedTick.triggerTick - world.time,
                    priority = orderedTick.priority,
                    registry = when (orderedTick.type) {
                        is Block -> Registries.BLOCK
                        is Fluid -> Registries.FLUID
                        else -> throw IllegalArgumentException("Unknown type ${orderedTick.type}")
                    } as Registry<T>
                ).apply {
                    this.priority.index
                }
            }
        }
    }

    data class BlockEventInfo(
        val pos: RelativeCoordinate,
        val type: Int,
        val data: Int,
        val block: Block
    ) {
        fun toRvcDataString(): String {
            return "${pos.x},${pos.y},${pos.z},$type,$data,${Registries.BLOCK.getId(block)}"
        }
    }


    fun splitCuboids(
        includeUntracked: Boolean = true,
    ): List<BlockBox> {
        class SplitingContext(
            var cuboid: BlockBox?,
            val points: SortedSet<BlockPos> = sortedSetOf()
        ) {
            constructor(points: Collection<BlockPos>) : this(
                null,
                points.toSortedSet()
            ) {
                shrinkCuboid()
            }

            fun shrinkCuboid() {
                if (points.isEmpty()) return
                val minX = points.minOf { it.x }
                val minY = points.minOf { it.y }
                val minZ = points.minOf { it.z }
                val maxX = points.maxOf { it.x }
                val maxY = points.maxOf { it.y }
                val maxZ = points.maxOf { it.z }
                cuboid = BlockBox(minX, minY, minZ, maxX, maxY, maxZ)
            }
        }

        val result: MutableList<SplitingContext>

        if (includeUntracked) {
            result = mutableListOf(SplitingContext(cachedPositions.keys))
            cachedIgnoredPositions.forEach { ignoredPos ->
                val iter = result.listIterator()
                while (iter.hasNext()) {
                    val entry = iter.next()
                    fun splitByAxis(
                        entry: SplitingContext,
                        axis: BlockPos.() -> Int
                    ) {
                        iter.add(
                            SplitingContext(
                                entry.points.filter { it.axis() < ignoredPos.key.axis() }
                            )
                        )
                        iter.add(
                            SplitingContext(
                                entry.points.filter { it.axis() > ignoredPos.key.axis() }
                            )
                        )
                    }
                    if (entry.cuboid?.contains(ignoredPos.key) == true) {
                        // Note: in this block element[i] is always removing
                        // select if we can split by an axis without add more cuboids
                        if (entry.points.none { it.x == ignoredPos.key.x }) {
                            iter.remove()
                            splitByAxis(entry) { x }
                        }
                        else if (entry.points.none { it.y == ignoredPos.key.y }) {
                            iter.remove()
                            splitByAxis(entry) { y }
                        }
                        else if (entry.points.none { it.z == ignoredPos.key.z }) {
                            iter.remove()
                            splitByAxis(entry) { z }
                        }
                        else {
                            var entryToSplit = entry
                            iter.remove()
                            // first, split by x
                            splitByAxis(entryToSplit) { x }
                            // then add same x points to the new cuboids
                            entryToSplit = SplitingContext(entryToSplit.points.filter { it.x == ignoredPos.key.x })
                            if (entryToSplit.cuboid?.contains(ignoredPos.key) != true) {
                                iter.add(entryToSplit)
                            }
                            // second, split by z
                            splitByAxis(entryToSplit) { z }
                            // then add same z points to the new cuboids
                            entryToSplit = SplitingContext(entryToSplit.points.filter { it.y == ignoredPos.key.y })
                            if (entryToSplit.cuboid?.contains(ignoredPos.key) != true) {
                                iter.add(entryToSplit)
                            }
                            // third, split by y
                            splitByAxis(entryToSplit) { y }
                        }
                    }
                }
                result.removeIf { it.points.isEmpty() || it.cuboid == null }
            }
        }
        else
            result = (cachedPositions.keys).map { SplitingContext(listOf(it)) }.toMutableList()

        return result.mapNotNull { it.cuboid }
    }

    open class SpreadEntry(
        val pos: BlockPos,
        val predicate: TrackPredicate,
        val mode: TrackPredicate.TrackMode,
        var structure: TrackedStructure?
    ) {
        fun spreadAround(
            world: World,
            successConsumer: (BlockPos) -> Unit,
            failConsumer: ((BlockPos) -> Unit)? = null
        ) {
            val x = pos.x
            val y = pos.y
            val z = pos.z
            val deltaRange = -predicate.distance..predicate.distance
            for (dx in deltaRange) {
                for (dy in deltaRange) {
                    for (dz in deltaRange) {
                        val pos = BlockPos(x + dx, y + dy, z + dz)
                        if (pos.getManhattanDistance(this.pos) > predicate.distance) continue
                        if (predicate.match(world, this.pos, pos, mode, structure!!)) {
                            successConsumer(pos)
                        }
                        else {
                            failConsumer?.invoke(pos)
                        }
                    }
                }
            }
        }
    }

    class TrackPoint(
        pos: RelativeCoordinate,
        predicate: TrackPredicate,
        mode: TrackPredicate.TrackMode,
        structure: TrackedStructure,
    ) : SpreadEntry(pos.blockPos(structure.origin), predicate, mode, structure) {
        val maxElements: Int
            get() = when (mode) {
                TrackPredicate.TrackMode.IGNORE -> 5000
                TrackPredicate.TrackMode.TRACK -> 8000
                TrackPredicate.TrackMode.NOOP -> 0
            }

        override fun toString(): String {
            return "TrackPoint(${pos.toShortString()}, $predicate, $mode)"
        }
    }

    fun onBlockAdded(pos: BlockPos) {
        dirty = true
        val trackPoint = Direction.entries.map(pos::offset).map { cachedIgnoredPositions[it] }.firstOrNull()
        if (trackPoint != null) {
            val readPos = mutableSetOf<BlockPos>()
            val queue = LinkedList<SpreadEntry>()
            queue.add(SpreadEntry(pos, trackPoint.predicate, trackPoint.mode, this))
            var maxElements = 8000
            while (queue.isNotEmpty() && maxElements > 0) {
                maxElements--
                val entry = queue.removeFirst()
                if (entry.pos in cachedIgnoredPositions || world.isAir(entry.pos)) continue
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        if (newPos in cachedPositions) return@spreadAround
                        if (!world.isAir(newPos)) {
                            cachedPositions[newPos] = trackPoint
                        }
                        queue.add(SpreadEntry(newPos, entry.predicate, trackPoint.mode, this))
                    }
                })
            }
        }
    }

    fun onBlockRemoved(pos: BlockPos) {
        dirty = true
        removeTrackpoint(pos)
        cachedPositions -= pos
    }

    init {
        io = RvcFileIO
    }

    override fun isInArea(pos: RelativeCoordinate): Boolean {
        return cachedPositions.contains(pos.blockPos(origin))
    }

    fun refreshPositions() {
        requireNotNull(networkWorker)
        if (!dirty) return
        cachedIgnoredPositions = hashMapOf()
        cachedPositions = hashMapOf()
        networkWorker.refreshPositions()
        dirty = false
        networkWorker.debugRender()
    }

    override val blockIterator: Iterator<RelativeCoordinate>
        get() =
            cachedPositions.keys.asSequence().map { getRelativeCoordinate(it) }.iterator()

    override fun clearArea() {
        clearSchedules()
        blockIterator.forEach {
            world.setBlockNoPP(it.blockPos(origin), Blocks.AIR.defaultState, 0)
        }
        blocks.keys.forEach {
            world.setBlockNoPP(it.blockPos(origin), Blocks.AIR.defaultState, 0)
        }
        entities.forEach {
            (world as? ClientWorld)?.entityLookup?.get(it.key)?.discard()
            (world as? ServerWorld)?.getEntity(it.key)?.discard()
        }
    }

    override fun paste() {
        blocks.forEach { (pos, state) ->
            world.setBlockNoPP(pos.blockPos(origin), state, 0)
        }
        blockEntities.forEach { (pos, nbt) ->
            world.getBlockEntity(pos.blockPos(origin))?.readNbt(nbt)
        }
        blocks.keys.forEach {
            world.markDirty(it.blockPos(origin))
        }
        entities.forEach {
            (world as? ServerWorld)?.getEntity(it.key)?.discard()
            val entity = EntityType.getEntityFromNbt(it.value, world).get()
            world.spawnEntity(entity)
            entity.refreshPositionAndAngles(
                entity.x + origin.x,
                entity.y + origin.y,
                entity.z + origin.z,
                entity.yaw,
                entity.pitch
            )
            if (world is ServerWorld) {
                (entity as? MobEntity)?.initialize(
                    world as ServerWorld,
                    world.getLocalDifficulty(entity.blockPos),
                    SpawnReason.STRUCTURE,
                    null,
                    it.value
                )
                (world as ServerWorld).spawnEntityAndPassengers(entity)
            }
            else {
                world.spawnEntity(entity)
            }
        }
        // todo
    }

    override fun setPlaced() {
        require(repository != null) { "Repository is null" }
        repository.placed = true
        repository.placementInfo = this.placementInfo
    }

    override fun startMoving() {
        require(repository != null) { "Repository is null" }
        repository.placed = false
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

    fun clearSchedules() {
        blockIterator.forEach { relative ->
            val pos = relative.blockPos(origin)
            (world as? ServerWorld)?.run {
                syncedBlockEventQueue.removeIf { it.pos == pos }
                val blockTickScheduler = getChunk(pos).blockTickScheduler as ChunkTickScheduler
                val fluidTickScheduler = getChunk(pos).fluidTickScheduler as ChunkTickScheduler
                blockTickScheduler.removeTicksIf { it.pos == pos }
                fluidTickScheduler.removeTicksIf { it.pos == pos }
            }
        }
    }

    fun collectSchedules() {
        blockEvents.clear()
        blockScheduledTicks.clear()
        fluidScheduledTicks.clear()

        (world as? ServerWorld)?.run {
            blockEvents.addAll(syncedBlockEventQueue.filter { isInArea(getRelativeCoordinate(it.pos)) }.map {
                BlockEventInfo(
                    pos = getRelativeCoordinate(it.pos),
                    block = it.block,
                    type = it.type,
                    data = it.data
                )
            })
            val chunks = cachedPositions.keys.asSequence()
                .map(ChunkPos::toLong)
                .toList().distinct()
                .map { getChunk(ChunkPos.getPackedX(it), ChunkPos.getPackedZ(it)) }
            val blockTickSchedulers = chunks.asSequence().map { it.blockTickScheduler as ChunkTickScheduler }
            val fluidTickSchedulers = chunks.asSequence().map { it.fluidTickScheduler as ChunkTickScheduler }
            blockScheduledTicks.addAll(blockTickSchedulers
                .flatMap { it.queuedTicks.filter { isInArea(getRelativeCoordinate(it.pos)) } }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    TickInfo.wrap(it, world) as TickInfo<Block>
                }
            )
            fluidScheduledTicks.addAll(fluidTickSchedulers
                .flatMap { it.queuedTicks.filter { isInArea(getRelativeCoordinate(it.pos)) } }
                .map {
                    @Suppress("UNCHECKED_CAST")
                    TickInfo.wrap(it, world) as TickInfo<Fluid>
                }
            )
        }
    }

    @Contract(pure = true)
    fun getRelativeCoordinate(pos: BlockPos): RelativeCoordinate {
        return RelativeCoordinate(pos.x - origin.x, pos.y - origin.y, pos.z - origin.z)
    }

    fun collectAllFromWorld() {
        blocks.clear()
        blockEntities.clear()
        entities.clear()
        refreshPositions()
        collectSchedules()
        val minPos = BlockPos.Mutable()
        val maxPos = BlockPos.Mutable()
        blockIterator.forEach { pos ->
            if (pos.x < minPos.x) minPos.x = pos.x
            if (pos.y < minPos.y) minPos.y = pos.y
            if (pos.z < minPos.z) minPos.z = pos.z
            if (pos.x > maxPos.x) maxPos.x = pos.x
            if (pos.y > maxPos.y) maxPos.y = pos.y
            if (pos.z > maxPos.z) maxPos.z = pos.z
            val state = world.getBlockState(pos.blockPos(origin))
            val beData = world.getBlockEntity(pos.blockPos(origin))?.createNbtWithId()
            blocks[pos] = state
            if (beData != null) blockEntities[pos] = beData
        }
        xSize = maxPos.x - minPos.x + 1
        ySize = maxPos.y - minPos.y + 1
        zSize = maxPos.z - minPos.z + 1
        world.getNonSpectatingEntities(Entity::class.java, Box.enclosing(minPos, maxPos)).asSequence()
            .filter {
                it !is PlayerEntity
            }.forEach {
                it.refreshPositionAndAngles(
                    it.x - origin.x,
                    it.y - origin.y,
                    it.z - origin.z,
                    it.yaw,
                    it.pitch
                )
                entities[it.uuid] = NbtCompound().apply(it::saveSelfNbt)
                it.refreshPositionAndAngles(
                    it.x + origin.x,
                    it.y + origin.y,
                    it.z + origin.z,
                    it.yaw,
                    it.pitch
                )
            }
    }

    fun removeTrackpoint(pos: BlockPos) {
        dirty = true
        val existing = trackPoints.find { it.pos == pos }
        if (existing != null) {
            cachedPositions.entries.removeIf { it.value == existing }
            trackPoints -= existing
        }
    }

    fun addTrackPoint(trackPoint: TrackPoint) {
        dirty = true
        removeTrackpoint(trackPoint.pos)
        trackPoints.add(trackPoint)
    }

    fun asCuboid(): IStructure {
        return object : IStructure {
            override var name = this@TrackedStructure.name
            override val xSize: Int get() = this@TrackedStructure.xSize
            override val ySize: Int get() = this@TrackedStructure.ySize
            override val zSize: Int get() = this@TrackedStructure.zSize
            override fun save(path: Path) {}
            override fun load(path: Path) {}
            override fun isInArea(pos: RelativeCoordinate): Boolean {
                return this@TrackedStructure.isInArea(
                    RelativeCoordinate(
                        pos.x + minX,
                        pos.y + minY,
                        pos.z + minZ
                    )
                )
            }

            override fun createPlacement(world: World, origin: BlockPos): IPlacement {
                return this@TrackedStructure
            }

            override fun getBlockEntityData(pos: RelativeCoordinate): NbtCompound? {
                return this@TrackedStructure.getBlockEntityData(
                    RelativeCoordinate(
                        pos.x + minX,
                        pos.y + minY,
                        pos.z + minZ
                    )
                )
            }

            override fun getBlockState(pos: RelativeCoordinate): BlockState {
                return this@TrackedStructure.getBlockState(RelativeCoordinate(pos.x + minX, pos.y + minY, pos.z + minZ))
            }

            override fun getOrCreateBlockEntityData(pos: RelativeCoordinate): NbtCompound {
                return this@TrackedStructure.getOrCreateBlockEntityData(
                    RelativeCoordinate(
                        pos.x + minX,
                        pos.y + minY,
                        pos.z + minZ
                    )
                )
            }

            override val entities = this@TrackedStructure.entities
        }
    }
}
