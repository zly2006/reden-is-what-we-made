package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.writeBlock
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.world.block.ChainRestrictedNeighborUpdater

/**
 * Code:
 *
 * ```java
 * 	public void updateNeighbor(BlockPos pos, Block sourceBlock, BlockPos sourcePos) {
 * 		BlockState blockState = this.world.getBlockState(pos);
 * 		this.updateNeighbor(blockState, pos, sourceBlock, sourcePos, false);
 * 	}
 * 	```
 * @see [ChainRestrictedNeighborUpdater.updateNeighbor]
 * @see [ChainRestrictedNeighborUpdater.SimpleEntry]
 *
 *
 */
open class StageBlockNCUpdate(
    parent: TickStage,
    entry: ChainRestrictedNeighborUpdater.SimpleEntry?,
    name: String
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.SimpleEntry>(name, parent),
NeighborChanged {
    constructor(parent: TickStage, entry: ChainRestrictedNeighborUpdater.SimpleEntry?)
            : this(parent, entry, "nc_update")

    final override lateinit var entry: ChainRestrictedNeighborUpdater.SimpleEntry
    init {
        if (entry != null) {
            this.entry = entry
        }
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        entry = ChainRestrictedNeighborUpdater.SimpleEntry(
            buf.readBlockPos(),
            buf.readBlock(),
            buf.readBlockPos()
        )
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(entry.pos)
        buf.writeBlock(entry.sourceBlock)
        buf.writeBlockPos(entry.sourcePos)
    }

    override val sourcePos: BlockPos
        get() = entry.sourcePos
    override val targetPos: BlockPos
        get() = entry.pos
    override val sourceBlock: Block
        get() = entry.sourceBlock
}
