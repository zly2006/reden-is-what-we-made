package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.ReadWriteStructure
import net.minecraft.block.Block
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
): ReadWriteStructure(name), IPlacement {
    override var xSize: Int = 0
    override var ySize: Int = 0
    override var zSize: Int = 0
    override var enabled: Boolean = true
    override val structure = this
    override lateinit var world: World
    override val origin: BlockPos.Mutable = BlockPos.ORIGIN.mutableCopy()
    override fun createPlacement(world: World, origin: BlockPos) = this
    val trackPoints = mutableListOf<TrackPoint>()
    val blockEvents = mutableListOf<BlockEvent>() // order sensitive
    val blockScheduledTicks = mutableListOf<NbtCompound>() // order sensitive
    val fluidScheduledTicks = mutableListOf<NbtCompound>() // order sensitive

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
        return trackPoints
            .firstOrNull { it.predicate.match(world, it.pos, pos) }?.mode?.isTrack()
            ?: false
    }

    val blockIterator: Iterator<BlockPos> get() = object: Iterator<BlockPos> {
        private val trackPointIter = trackPoints.asSequence().filter { it.mode == TrackPoint.TrackMode.TRACK }.iterator()
        val readPos = hashSetOf<BlockPos>()
        val ignored = hashSetOf<BlockPos>()
        val currentPointPoses = hashSetOf<BlockPos>()

        init {
            // add ignored blocks
            val queue = trackPoints.filter { it.mode == TrackPoint.TrackMode.IGNORE }.map { SpreadEntry(it.pos, it.predicate) }.toMutableList()
            var maxElements = 100000
            while (queue.isNotEmpty() && maxElements > 0) {
                val entry = queue.removeFirst()
                entry.spreadAround(world, { pos ->
                    if (ignored.add(pos)) {
                        maxElements--
                        queue.add(SpreadEntry(pos, entry.predicate))
                    }
                })
            }
        }

        override fun hasNext() = currentPointPoses.isNotEmpty() // still have some poses to read
                || trackPointIter.hasNext()

        override fun next(): BlockPos {
            if (currentPointPoses.isEmpty()) {
                // first, add all blocks recursively
                val queue = LinkedList<SpreadEntry>()
                queue.add(trackPointIter.next())
                var maxElements = 1000
                while (queue.isNotEmpty() && maxElements > 0) {
                    val entry = queue.removeFirst()
                    if (entry.pos in ignored) continue
                    entry.spreadAround(world, { newPos ->
                        if (readPos.add(newPos)) {
                            currentPointPoses.add(newPos)
                            maxElements--
                            queue.add(SpreadEntry(newPos, entry.predicate))
                        }
                    })
                }
            }

            val iter = currentPointPoses.iterator()
            val pos = iter.next()
            iter.remove()
            return pos
        }
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