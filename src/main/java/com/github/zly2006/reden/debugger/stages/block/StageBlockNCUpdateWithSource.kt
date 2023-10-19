package com.github.zly2006.reden.debugger.stages.block
import com.github.zly2006.reden.debugger.TickStage
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
    override val entry: ChainRestrictedNeighborUpdater.StatefulEntry
): AbstractBlockUpdateStage<ChainRestrictedNeighborUpdater.StatefulEntry>("nc_update_with_source", parent) {
}
