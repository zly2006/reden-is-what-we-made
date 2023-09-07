package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.render.BlockBorder
import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.PositionIterable
import com.github.zly2006.reden.rvc.ReadWriteStructure
import com.github.zly2006.reden.utils.setBlockNoPP
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.fluid.Fluid
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.server.world.BlockEvent
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.tick.ChunkTickScheduler
import net.minecraft.world.tick.Tick
import java.util.*

class TrackedStructure (
    name: String
): ReadWriteStructure(name), IPlacement, PositionIterable {
    override var xSize: Int = 0
    override var ySize: Int = 0
    override var zSize: Int = 0
    override var enabled: Boolean = true
    override val structure = this
    override lateinit var world: World
    override val origin: BlockPos.Mutable = BlockPos.ORIGIN.mutableCopy()
    override fun createPlacement(world: World, origin: BlockPos) = this
    internal var cachedPositions = mutableMapOf<BlockPos, TrackPoint>()
    internal var cachedIgnoredPositions = mutableMapOf<BlockPos, TrackPoint>()
    val trackPoints = mutableListOf<TrackPoint>()
    val blockEvents = mutableListOf<BlockEvent>() // order sensitive
    val blockScheduledTicks = mutableListOf<NbtCompound>() // order sensitive
    val fluidScheduledTicks = mutableListOf<NbtCompound>() // order sensitive

    fun debugRender() {
        cachedPositions.forEach {
            if (!world.isAir(it.key))
                BlockBorder[it.key] = 1
        }
        cachedIgnoredPositions.forEach {
            if (!world.isAir(it.key))
                BlockBorder[it.key] = 2
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

    fun splitCuboids(): List<BlockBox> {
        class IncludeEntry(
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

        val result = mutableListOf(IncludeEntry(cachedPositions.keys))
        cachedIgnoredPositions.forEach { ignored ->
            val iter = result.listIterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                if (entry.cuboid?.contains(ignored.key) == true) {
                    // Note: in this block element[i] is always removing
                    // select if we can split by an axis without add more cuboids
                    if (entry.points.none { it.x == ignored.key.x }) {
                        iter.remove()
                        // split by x
                        iter.add(
                            IncludeEntry(
                                entry.points.filter { it.x < ignored.key.x }
                            )
                        )
                        iter.add(
                            IncludeEntry(
                                entry.points.filter { it.x > ignored.key.x }
                            )
                        )
                    }
                    else if (entry.points.none { it.y == ignored.key.y }) {
                        iter.remove()
                        // split by y
                        iter.add(
                            IncludeEntry(
                                entry.points.filter { it.y < ignored.key.y }
                            )
                        )
                        iter.add(
                            IncludeEntry(
                                entry.points.filter { it.y > ignored.key.y }
                            )
                        )
                    }
                    else if (entry.points.none { it.z == ignored.key.z }) {
                        iter.remove()
                        // split by z
                        iter.add(
                            IncludeEntry(
                                entry.points.filter { it.z < ignored.key.z }
                            )
                        )
                        iter.add(
                            IncludeEntry(
                                entry.points.filter { it.z > ignored.key.z }
                            )
                        )
                    }
                    else {
                        var entryToSplit = entry
                        iter.remove()
                        // first, split by x
                        iter.add(
                            IncludeEntry(
                                entryToSplit.points.filter { it.x < ignored.key.x }
                            )
                        )
                        iter.add(
                            IncludeEntry(
                                entryToSplit.points.filter { it.x > ignored.key.x }
                            )
                        )
                        // then add same x points to the new cuboids
                        entryToSplit = IncludeEntry(entryToSplit.points.filter { it.x == ignored.key.x })
                        if (entryToSplit.cuboid?.contains(ignored.key) != true) {
                            iter.add(entryToSplit)
                        }
                        // second, split by y
                        iter.add(
                            IncludeEntry(
                                entryToSplit.points.filter { it.y < ignored.key.y }
                            )
                        )
                        iter.add(
                            IncludeEntry(
                                entryToSplit.points.filter { it.y > ignored.key.y }
                            )
                        )
                        // then add same y points to the new cuboids
                        entryToSplit = IncludeEntry(entryToSplit.points.filter { it.y == ignored.key.y })
                        if (entryToSplit.cuboid?.contains(ignored.key) != true) {
                            iter.add(entryToSplit)
                        }
                        // third, split by z
                        iter.add(
                            IncludeEntry(
                                entryToSplit.points.filter { it.z < ignored.key.z }
                            )
                        )
                        iter.add(
                            IncludeEntry(
                                entryToSplit.points.filter { it.z > ignored.key.z }
                            )
                        )
                        // then add same z point is ignored
                    }
                }
            }
            result.removeIf { it.points.isEmpty() || it.cuboid == null }
        }

        return result.map { it.cuboid!! }
    }

    open class SpreadEntry(
        val pos: BlockPos,
        val predicate: TrackPredicate
    ) {

        fun spreadAround(world: World, successConsumer: (BlockPos) -> Unit, failConsumer: ((BlockPos) -> Unit)? = null) {
            val x = pos.x
            val y = pos.y
            val z = pos.z
            val deltaRange = -predicate.distance .. predicate.distance
            for (dx in deltaRange) {
                for (dy in deltaRange) {
                    for (dz in deltaRange) {
                        val pos = BlockPos(x + dx, y + dy, z + dz)
                        if (predicate.match(world, this.pos, pos)) {
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
        pos: BlockPos,
        predicate: TrackPredicate,
        val mode: TrackMode,
    ): SpreadEntry(pos, predicate) {
        enum class TrackMode {
            NOOP,
            TRACK,
            IGNORE;

            fun isTrack(): Boolean {
                return this == TRACK
            }
        }
    }

    fun onBlockAdded(pos: BlockPos) {
    }

    fun onBlockRemoved(pos: BlockPos) {
    }

    init {
        io = RvcFileIO
    }

    override fun isInArea(pos: BlockPos): Boolean {
        return cachedPositions.contains(pos)
    }

    fun refreshPositions() {
        cachedIgnoredPositions.clear()
        cachedPositions.clear()
        val readPos = hashSetOf<BlockPos>()

        trackPoints.asSequence().filter { it.mode == TrackPoint.TrackMode.IGNORE }.forEach { trackPoint ->
            // first, add all blocks recursively
            val queue = LinkedList<SpreadEntry>()
            queue.add(trackPoint)
            var maxElements = 100000
            while (queue.isNotEmpty() && maxElements > 0) {
                val entry = queue.removeFirst()
                if (entry.pos in cachedIgnoredPositions || world.isAir(entry.pos)) continue
                cachedIgnoredPositions[entry.pos] = trackPoint
                entry.spreadAround(world, { newPos ->
                    if (readPos.add(newPos)) {
                        maxElements--
                        queue.add(SpreadEntry(newPos, entry.predicate))
                    }
                })
            }
        }

        trackPoints.asSequence().filter { it.mode == TrackPoint.TrackMode.TRACK }.forEach { trackPoint ->
            // first, add all blocks recursively
            val queue = LinkedList<SpreadEntry>()
            queue.add(trackPoint)
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
                        queue.add(SpreadEntry(newPos, entry.predicate))
                    }
                })
            }
        }
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
            blockEvents.addAll(syncedBlockEventQueue.filter { isInArea(it.pos) })
            val chunks = blockIterator.asSequence()
                .map(ChunkPos::toLong)
                .distinct()
                .map { getChunk(ChunkPos.getPackedX(it), ChunkPos.getPackedZ(it)) }
            val time = world.levelProperties.time
            val blockTickSchedulers = chunks.map { it.blockTickScheduler as ChunkTickScheduler }
            val fluidTickSchedulers = chunks.map { it.fluidTickScheduler as ChunkTickScheduler }
            blockTickSchedulers
                .flatMap { it.queuedTicks.filter { isInArea(it.pos) } }
                .map { Tick.orderedTickToNbt(it, { Registries.BLOCK.getId(it as Block).toString() }, time) }
                .let { blockScheduledTicks.addAll(it) }
            fluidTickSchedulers
                .flatMap { it.queuedTicks.filter { isInArea(it.pos) } }
                .map { Tick.orderedTickToNbt(it, { Registries.FLUID.getId(it as Fluid).toString() }, time) }
                .let { fluidScheduledTicks.addAll(it) }
        }
    }

    fun addTrackPoint(trackPoint: TrackPoint) {
        trackPoints.removeIf { it.pos == trackPoint.pos }
        trackPoints.add(trackPoint)
        refreshPositions()
    }

    enum class TrackPredicate(val distance: Int, val same: Boolean) {
        SAME(2, true),
        NEAR(1, false),
        QC(2, false),
        FAR(3, false);

        fun match(world: World, pos1: BlockPos, pos2: BlockPos): Boolean {
            val distance = pos1.getManhattanDistance(pos2)
            return distance <= this.distance &&
                    (!this.same || world.getBlockState(pos1).block == world.getBlockState(pos2).block)
        }
    }
}