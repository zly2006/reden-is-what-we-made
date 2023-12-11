package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.readBlockState
import com.github.zly2006.reden.utils.readDirection
import com.github.zly2006.reden.utils.writeBlockState
import com.github.zly2006.reden.utils.writeDirection
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

/**
 * Code:
 *
 * ```java
 * 	public void replaceWithStateForNeighborUpdate(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth) {
 * 		NeighborUpdater.replaceWithStateForNeighborUpdate(this.world, direction, neighborState, pos, neighborPos, flags, maxUpdateDepth - 1);
 * 	}
 * 	```
 * @see [ChainRestrictedNeighborUpdater.replaceWithStateForNeighborUpdate]
 * @see [ChainRestrictedNeighborUpdater.StateReplacementEntry]
 */
class StageBlockPPUpdate(
    parent: TickStage,
    entry: ChainRestrictedNeighborUpdater.StateReplacementEntry?
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.StateReplacementEntry>("pp_update", parent) {
    override lateinit var entry: ChainRestrictedNeighborUpdater.StateReplacementEntry
    init {
        if (entry != null) {
            this.entry = entry
        }
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        entry = ChainRestrictedNeighborUpdater.StateReplacementEntry(
            buf.readDirection(),
            buf.readBlockState(),
            buf.readBlockPos(),
            buf.readBlockPos(),
            buf.readInt(),
            buf.readInt()
        )
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeDirection(entry.direction)
        buf.writeBlockState(entry.neighborState)
        buf.writeBlockPos(entry.pos)
        buf.writeBlockPos(entry.neighborPos)
        buf.writeInt(entry.updateFlags)
        buf.writeInt(entry.updateLimit)
    }

    override val sourcePos: BlockPos
        get() = entry.neighborPos
    override val targetPos: BlockPos
        get() = entry.pos
    override val sourceBlock: Block
        get() = entry.neighborState.block
}
