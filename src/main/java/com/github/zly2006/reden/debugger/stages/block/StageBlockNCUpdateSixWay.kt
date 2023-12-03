package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
import com.github.zly2006.reden.utils.readBlock
import com.github.zly2006.reden.utils.readDirection
import com.github.zly2006.reden.utils.writeBlock
import com.github.zly2006.reden.utils.writeDirection
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.MutableText
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
    parent: TickStage,
    entry: ChainRestrictedNeighborUpdater.SixWayEntry?
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.SixWayEntry>("nc_update_6", parent),
NeighborChanged {
    override lateinit var entry: ChainRestrictedNeighborUpdater.SixWayEntry
    var lastTickedDirectionIndex = -1

    override val displayName: MutableText
        get() = super.displayName.append(" (")
            .append(Direction.byId(entry.currentDirectionIndex).name)
            .append(" except ")
            .append(entry.except?.asString() ?: "null")
            .append(")")

    override fun tick() {
        lastTickedDirectionIndex = entry.currentDirectionIndex
        super.tick()
    }

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
        lastTickedDirectionIndex = buf.readVarInt()
    }

    override fun writeByteBuf(buf: PacketByteBuf) {
        super.writeByteBuf(buf)
        buf.writeBlockPos(entry.pos)
        buf.writeBlock(entry.sourceBlock)
        buf.writeNullable(entry.except, PacketByteBuf::writeDirection)
        buf.writeVarInt(entry.currentDirectionIndex)
        buf.writeVarInt(lastTickedDirectionIndex)
    }

    fun getNextUpdate(): Direction {
        if (NeighborUpdater.UPDATE_ORDER[entry.currentDirectionIndex] == entry.except) {
            ++entry.currentDirectionIndex
        }
        return NeighborUpdater.UPDATE_ORDER[entry.currentDirectionIndex]
    }

    fun getPrevUpdate(): Direction? {
        return NeighborUpdater.UPDATE_ORDER.getOrNull(lastTickedDirectionIndex)
    }

    override val sourcePos: BlockPos
        get() = entry.pos
    override val targetPos: BlockPos?
        get() = getPrevUpdate()?.let { entry.pos.offset(it) }
    override val sourceBlock: Block
        get() = entry.sourceBlock
}
