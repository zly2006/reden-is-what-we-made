package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.render.BlockOutline
import com.github.zly2006.reden.rvc.*
import com.github.zly2006.reden.utils.setBlockNoPP
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.NetworkSide
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.world.ServerChunkManager
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.*
import net.minecraft.world.World
import net.minecraft.world.tick.ChunkTickScheduler
import net.minecraft.world.tick.TickPriority
import java.util.*

/**
 * todo: see #81
 */
class TrackedStructure(
    name: String,
    var side: NetworkSide
) : ReadWriteStructure(name), IPlacement, PositionIterable {
    override var xSize: Int = 0
    override var ySize: Int = 0
    override var zSize: Int = 0
    override var enabled: Boolean = true
    override val structure = this
    override lateinit var world: World
    lateinit var placementInfo: PlacementInfo
    override val origin: BlockPos get() = placementInfo.origin
    override fun createPlacement(world: World, origin: BlockPos) = this
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

    fun debugRender() {
        if (side == NetworkSide.SERVERBOUND) return
        BlockOutline.blocks.clear()
        BlockBorder.tags.clear()
        cachedPositions.forEach {
            if (!world.isAir(it.key))
                BlockOutline.blocks[it.key] = world.getBlockState(it.key)
        }
        trackPoints.forEach {
            if (!world.isAir(it.pos))
                BlockBorder[it.pos] = if (it.mode.isTrack()) 1 else 2
        }
    }

    val blockBox: BlockBox
        get() {
            val minX = cachedPositions.keys.minOf { it.x }
            val minY = cachedPositions.keys.minOf { it.y }
            val minZ = cachedPositions.keys.minOf { it.z }
            val maxX = cachedPositions.keys.maxOf { it.x }
            val maxY = cachedPositions.keys.maxOf { it.y }
            val maxZ = cachedPositions.keys.maxOf { it.z }
            return BlockBox(minX, minY, minZ, maxX, maxY, maxZ)
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
                        } else if (entry.points.none { it.y == ignoredPos.key.y }) {
                            iter.remove()
                            splitByAxis(entry) { y }
                        } else if (entry.points.none { it.z == ignoredPos.key.z }) {
                            iter.remove()
                            splitByAxis(entry) { z }
                        } else {
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
        } else
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
                        } else {
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
    }

    fun onBlockAdded(pos: BlockPos) {
        dirty = true
        val trackPoint = Direction.values().map(pos::offset).map { cachedIgnoredPositions[it] }.firstOrNull()
        if (trackPoint != null) {
            val readPos = mutableSetOf<BlockPos>()
            val queue = LinkedList<SpreadEntry>()
            queue.add(SpreadEntry(pos, trackPoint.predicate, trackPoint.mode, this))
            var maxElements = 80000
            while (queue.isNotEmpty() && maxElements > 0) {
                val entry = queue.removeFirst()
                if (entry.pos in cachedIgnoredPositions || world.isAir(entry.pos)) continue
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        if (newPos in cachedPositions) return@spreadAround
                        if (!world.isAir(newPos)) {
                            cachedPositions[newPos] = trackPoint
                        }
                        maxElements--
                        queue.add(SpreadEntry(newPos, entry.predicate, trackPoint.mode, this))
                    }
                })
            }
        }
    }

    fun onBlockRemoved(pos: BlockPos) {
        dirty = true
        val trackPoint = trackPoints.find { it.pos == pos }
        if (trackPoint != null) {
            trackPoints.remove(trackPoint)
        }
    }

    init {
        io = RvcFileIO
    }

    override fun isInArea(pos: BlockPos): Boolean {
        return cachedPositions.contains(pos)
    }

    fun refreshPositions() {
        if (!dirty) return
        val timeStart = System.currentTimeMillis()
        cachedIgnoredPositions.clear()
        cachedPositions.clear()
        val readPos = hashSetOf<BlockPos>()

        val airCache = hashSetOf<BlockPos>()
        fun World.air(pos: BlockPos): Boolean {
            val chunkPos = ChunkPos(pos)
            return airCache.contains(pos) || if (isClient) {
                isAir(pos)
            } else {
                (chunkManager as ServerChunkManager).threadedAnvilChunkStorage.currentChunkHolders[chunkPos.toLong()]?.let {
                    it.worldChunk?.getBlockState(pos)?.isAir ?: true
                } ?: true
            }
        }

        trackPoints.asSequence().filter { it.mode == TrackPredicate.TrackMode.IGNORE }.forEach { trackPoint ->
            // first, add all blocks recursively
            val queue = LinkedList<SpreadEntry>()
            queue.add(trackPoint)
            var maxElements = 100000
            while (queue.isNotEmpty() && maxElements > 0) {
                val entry = queue.removeFirst()
                if (entry.pos in cachedIgnoredPositions) continue
                if (world.air(entry.pos)) {
                    airCache.add(entry.pos)
                    continue
                }
                cachedIgnoredPositions[entry.pos] = trackPoint
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        maxElements--
                        queue.add(SpreadEntry(newPos, entry.predicate, trackPoint.mode, this))
                    }
                })
            }
        }

        trackPoints.asSequence().filter { it.mode == TrackPredicate.TrackMode.TRACK }.forEach { trackPoint ->
            // first, add all blocks recursively
            val queue = LinkedList<SpreadEntry>()
            queue.add(trackPoint)
            var maxElements = 80000
            while (queue.isNotEmpty() && maxElements > 0) {
                val entry = queue.removeFirst()
                if (entry.pos in cachedIgnoredPositions) continue
                if (world.air(entry.pos)) {
                    airCache.add(entry.pos)
                    continue
                }
                if (!trackPoint.pos.isWithinDistance(entry.pos, 200.0)) {
                    Reden.LOGGER.error("Track point ${trackPoint.pos} is too far away from ${entry.pos}")
                    continue
                }
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        if (newPos in cachedPositions) return@spreadAround
                        if (!world.air(newPos)) {
                            cachedPositions[newPos] = trackPoint
                        }
                        maxElements--
                        queue.add(SpreadEntry(newPos, entry.predicate, trackPoint.mode, this))
                    }
                })
            }
        }
        val timeEnd = System.currentTimeMillis()
        // todo:debug
        println("refreshPositions: ${timeEnd - timeStart}ms")
        debugRender()
        dirty = false
    }

    override val blockIterator: Iterator<BlockPos> get() = cachedPositions.keys.iterator()

    override fun clearArea() {
        clearSchedules()
        blockIterator.forEach { pos ->
            world.setBlockNoPP(pos, Blocks.AIR.defaultState, 0)
        }
    }

    override fun paste() {
        clearArea()
        super.paste()
    }

    fun clearSchedules() {
        blockIterator.forEach { pos ->
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
            blockEvents.addAll(syncedBlockEventQueue.filter { isInArea(it.pos) }.map { BlockEventInfo(
                pos = getRelativeCoordinate(it.pos),
                block = it.block,
                type = it.type,
                data = it.data
            ) })
            val chunks = blockIterator.asSequence()
                .map(ChunkPos::toLong)
                .toList().distinct()
                .map { getChunk(ChunkPos.getPackedX(it), ChunkPos.getPackedZ(it)) }
            val time = world.levelProperties.time
            val blockTickSchedulers = chunks.asSequence().map { it.blockTickScheduler as ChunkTickScheduler }
            val fluidTickSchedulers = chunks.asSequence().map { it.fluidTickScheduler as ChunkTickScheduler }
            blockScheduledTicks.addAll(blockTickSchedulers
                .flatMap { it.queuedTicks.filter { isInArea(it.pos) } }
                .map { TickInfo(
                    pos = getRelativeCoordinate(it.pos),
                    type = it.type as Block,
                    delay = it.triggerTick - world.time,
                    priority = it.priority,
                    registry = Registries.BLOCK
                ) }
            )
            fluidScheduledTicks.addAll(fluidTickSchedulers
                .flatMap { it.queuedTicks.filter { isInArea(it.pos) } }
                .map { TickInfo(
                    pos = getRelativeCoordinate(it.pos),
                    type = it.type as Fluid,
                    delay = it.triggerTick - world.time,
                    priority = it.priority,
                    registry = Registries.FLUID
                ) }
            )
        }
    }

    fun getRelativeCoordinate(pos: BlockPos): RelativeCoordinate {
        return RelativeCoordinate(pos.x - origin.x, pos.y - origin.y, pos.z - origin.z)
    }

    fun collectFromWorld() {
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
            val state = world.getBlockState(pos)
            val beData = world.getBlockEntity(pos)?.createNbtWithId()
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
                entities[it.uuid] = it.writeNbt(NbtCompound())
            }
    }

    fun detectOrigin(): BlockPos? {
        var minX = Int.MAX_VALUE
        var minY = Int.MAX_VALUE
        var minZ = Int.MAX_VALUE
        cachedPositions.keys.forEach {
            if (it.x < minX) minX = it.x
            if (it.y < minY) minY = it.y
            if (it.z < minZ) minZ = it.z
        }
        if (minX == Int.MAX_VALUE || minY == Int.MAX_VALUE || minZ == Int.MAX_VALUE) return null
        return BlockPos(minX, minY, minZ)
    }

    fun addTrackPoint(trackPoint: TrackPoint) {
        dirty = true
        trackPoints.removeIf { it.pos == trackPoint.pos }
        trackPoints.add(trackPoint)
        refreshPositions()
    }
}
