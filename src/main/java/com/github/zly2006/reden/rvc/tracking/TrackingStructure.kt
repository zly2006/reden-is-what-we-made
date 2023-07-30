package com.github.zly2006.reden.rvc.tracking

import com.github.zly2006.reden.rvc.IPlacement
import com.github.zly2006.reden.rvc.IStructure
import com.github.zly2006.reden.rvc.ReadWriteStructure
import com.github.zly2006.reden.setBlockNoPP
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap
import net.minecraft.block.Block
import net.minecraft.util.collection.IdList
import net.minecraft.util.math.BlockPos
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
    @JvmField val trackingPositions = object: LinkedHashSet<BlockPos>() {
        override fun add(element: BlockPos): Boolean {
            if (element is BlockPos.Mutable) {
                throw IllegalArgumentException("element must be immutable")
            }
            return super.add(element)
        }
    }

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
    override fun isInArea(pos: BlockPos) = pos in blocks
}
