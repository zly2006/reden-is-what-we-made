package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
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
    override val entry: ChainRestrictedNeighborUpdater.StateReplacementEntry
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.StateReplacementEntry>("nc_update", parent) {
}
