package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.readBlockState
import com.github.zly2006.reden.utils.writeBlock
import com.github.zly2006.reden.utils.writeBlockState
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

/**
 * Code:
 * ```java
 *  void updateNeighbors(BlockPos pos, Block sourceBlock, @Nullable Direction except) {
 * 		for(int i = 0; i < UPDATE_ORDER.length; ++i) {
 * 			Direction direction = UPDATE_ORDER[i];
 * 			if (direction != except) {
 * 				this.updateNeighbor(pos.offset(direction), sourceBlock, pos);
 * 			}
 * 		}
 * 	}
 * ```
 * @see [ChainRestrictedNeighborUpdater.updateNeighbor]
 * @see [ChainRestrictedNeighborUpdater.StatefulEntry]
 */
class StageBlockNCUpdateWithSource(
    parent: TickStage,
    entry: ChainRestrictedNeighborUpdater.StatefulEntry?
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.StatefulEntry>("nc_update_with_source", parent),
NeighborChanged {
    override lateinit var entry: ChainRestrictedNeighborUpdater.StatefulEntry
    init {
        if (entry != null) {
            this.entry = entry
        }
    }
    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockState(entry.state)
        buf.writeBlockPos(entry.pos)
        buf.writeBlock(entry.sourceBlock)
        buf.writeBlockPos(entry.sourcePos)
        buf.writeBoolean(entry.movedByPiston)
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        entry = ChainRestrictedNeighborUpdater.StatefulEntry(
            buf.readBlockState(),
            buf.readBlockPos(),
            buf.readBlock(),
            buf.readBlockPos(),
            buf.readBoolean()
        )
    }
    override val sourcePos: BlockPos
        get() = entry.sourcePos
    override val targetPos: BlockPos
        get() = entry.pos
    override val sourceBlock: Block
        get() = entry.sourceBlock
}
