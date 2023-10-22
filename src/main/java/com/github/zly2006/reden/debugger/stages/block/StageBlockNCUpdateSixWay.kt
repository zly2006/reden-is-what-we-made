package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.stages.UpdateBlockStage
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.readDirection
import com.github.zly2006.reden.utils.writeBlock
import com.github.zly2006.reden.utils.writeDirection
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.block.ChainRestrictedNeighborUpdater
import net.minecraft.world.block.NeighborUpdater

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
 * @see [ChainRestrictedNeighborUpdater.SixWayEntry]
 *
 *
 */
class StageBlockNCUpdateSixWay(
    parent: UpdateBlockStage,
    entry: ChainRestrictedNeighborUpdater.SixWayEntry?
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.SixWayEntry>("nc_update_6", parent) {
    override lateinit var entry: ChainRestrictedNeighborUpdater.SixWayEntry
    init {
        if (entry != null) {
            this.entry = entry
        }
    }

    override fun readByteBuf(buf: PacketByteBuf) {
        super.readByteBuf(buf)
        entry = ChainRestrictedNeighborUpdater.SixWayEntry(
            buf.readBlockPos(),
            buf.readBlock(),
            buf.readNullable(PacketByteBuf::readDirection)
        )
        entry.currentDirectionIndex = buf.readVarInt()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(entry.pos)
        buf.writeBlock(entry.sourceBlock)
        buf.writeNullable(entry.except, PacketByteBuf::writeDirection)
        buf.writeVarInt(entry.currentDirectionIndex)
    }

    fun getNextUpdate(): Direction {
        if (NeighborUpdater.UPDATE_ORDER[entry.currentDirectionIndex] == entry.except) {
            ++entry.currentDirectionIndex
        }
        return NeighborUpdater.UPDATE_ORDER[entry.currentDirectionIndex]
    }
    override val sourcePos: BlockPos
        get() = entry.pos
    override val targetPos: BlockPos
        get() = entry.pos.offset(getNextUpdate())
}
