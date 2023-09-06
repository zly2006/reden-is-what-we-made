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
    private var cachedPositions = mutableMapOf<BlockPos, TrackPoint>()
    private var cachedIgnoredPositions = mutableMapOf<BlockPos, TrackPoint>()
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
                        cachedPositions[newPos] = trackPoint
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

    enum class TrackPredicate(val distance: Int, val same: Boolean) {
        SAME(1, true),
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