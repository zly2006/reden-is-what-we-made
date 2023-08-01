package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.ReadWriteStructure
import com.github.zly2006.reden.utils.isDepending
import com.github.zly2006.reden.utils.setBlockNoPP
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.util.collection.IdList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.nio.file.Path

/**
 * Only used for tracking changes, exporting and showing changes when rolling back changes.
 * It should NOT be used either for version control or save data/network.
 */
class TrackingStructure(
    name: String
): ReadWriteStructure(name), IPlacement {
    var children = mutableListOf<IdList<TrackingStructure>>()
    override var enabled: Boolean = true
    override val structure: IStructure = this
    override lateinit var world: World
    override var xSize: Int = 0
    override var ySize: Int = 0
    override var zSize: Int = 0
    override val origin: BlockPos.Mutable = BlockPos.ORIGIN.mutableCopy()
    val diffs = Long2ObjectRBTreeMap<TrackedDiff>()
    private val trackingPositions = mutableSetOf<BlockPos>()
    val tracking: Sequence<BlockPos>
        get() = trackingPositions.asSequence()

    override fun createPlacement(world: World, origin: BlockPos): TrackingStructure {
        if (world != this.world || origin != this.origin) {
            throw IllegalArgumentException("world and origin must be the same as the structure")
        }
        return this
    }

    override fun clearArea() {
        blocks.keys.forEach { world.removeBlock(it, false) }
    }

    override fun paste() {
        blocks.forEach { (pos, state) ->
            world.setBlockNoPP(pos, state, Block.NOTIFY_LISTENERS)
        }
    }

    override fun save(path: Path) = throw UnsupportedOperationException("TrackingStructure is not used for saving data.")
    override fun load(path: Path) = throw UnsupportedOperationException("TrackingStructure is not used for saving data.")
    override fun isInArea(pos: BlockPos) = pos in trackingPositions

    fun onBlockUpdate(pos: BlockPos) {
        if (trackingPositions.contains(pos)) return
    }

    interface ISpreadMode {
        fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean?
    }

    enum class SpreadMode: ISpreadMode {
        SAME {
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState) =
                state1.block == state2.block
        },
        SOLID {
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState) =
                state1.isSolidBlock(world, pos1) && state2.isSolidBlock(world, pos2)
        },
        DEPENDING {
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                return (state1.isDepending && pos1.down() == pos2) ||
                        (state2.isDepending && pos2.down() == pos1)
            }
        },
        CONNECTING {
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                TODO("Not yet implemented")
            }
                   },
        QC{
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                TODO("Not yet implemented")
            }
          },
        UPDATING{
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                TODO("Not yet implemented")
            }
                },
        AIR_1{
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                TODO("Not yet implemented")
            }
             },
        AIR_2{
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                TODO("Not yet implemented")
            }
             },
        AIR_3{
            override fun canSpread(world: World, pos1: BlockPos, state1: BlockState, pos2: BlockPos, state2: BlockState): Boolean? {
                TODO("Not yet implemented")
            }
             },
    }

    class SpreadResult {
        enum class Mode {
            NONE, ADD, REMOVE
        }
        val blocks = mutableSetOf<BlockPos>()
        val mode = Mode.NONE
    }

    private fun spreadBlock(pos: BlockPos, mode: SpreadMode) {
        Direction.values().forEach { spreadBlockWithWay(pos, mode, it) }
    }

    private fun isValidPos(pos: BlockPos) =
        world.worldBorder.contains(pos) && pos.y >= world.bottomY && pos.y < world.topY

    private fun spreadBlockWithWay(pos: BlockPos, mode: SpreadMode, direction: Direction) {

    }

    private fun trackBlock(pos: BlockPos) {
        @Suppress("NAME_SHADOWING")
        val pos = pos.toImmutable()
        trackingPositions.add(pos)
    }
}
