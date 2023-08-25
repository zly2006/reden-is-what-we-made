package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.ReadWriteStructure
import com.github.zly2006.reden.rvc.io.RvcFileIO
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

    class TrackPoint(
        val pos: BlockPos,
        val predicate: TrackPredicate,
        val mode: TrackMode,
    ) {
        enum class TrackMode {
            NOOP,
            TRACK,
            IGNORE;

            fun isTrack(): Boolean {
                return this == TRACK
            }
        }
        enum class TrackPredicate(distance: Int, same: Boolean) {
            SAME(1, true),
            NEAR(1, false),
            QC(2, false),
            FAR(3, false);

            fun match(world: World, pos1: BlockPos, pos2: BlockPos): Boolean {
                val distance = pos1.getManhattanDistance(pos2)
                return when (this) {
                    SAME -> distance == 1 && world.getBlockState(pos1).block == world.getBlockState(pos2).block
                    NEAR -> distance <= 1
                    QC -> distance <= 2
                    FAR -> distance <= 3
                }
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

    val blockIterator: Iterator<BlockPos> get() {
        TODO()
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
}