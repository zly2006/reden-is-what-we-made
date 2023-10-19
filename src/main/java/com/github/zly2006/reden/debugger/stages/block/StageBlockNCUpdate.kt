package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
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
class StageBlockNCUpdate(
    parent: TickStage,
    override val entry: ChainRestrictedNeighborUpdater.SimpleEntry
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.SimpleEntry>("nc_update", parent) {
}
