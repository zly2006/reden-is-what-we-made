package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.access.WorldData;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.world.BlockEntitiesRootStage;
import com.github.zly2006.reden.debugger.stages.world.BlockEntityStage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(value = World.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorld implements WorldAccess, AutoCloseable {
    @Shadow public abstract Profiler getProfiler();

    @Shadow private boolean iteratingTickingBlockEntities;

    @Shadow @Final private List<BlockEntityTickInvoker> pendingBlockEntityTickers;

    @Shadow @Final protected List<BlockEntityTickInvoker> blockEntityTickers;

    @Shadow public abstract boolean shouldTickBlockPos(BlockPos pos);

    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public void tickBlockEntities() {
        Profiler profiler = this.getProfiler();
        Iterator<BlockEntityTickInvoker> iterator = null;
        BlockEntityTickInvoker blockEntityTickInvoker;
        // Leave local captures here

        if ((Object)this instanceof ServerWorld) {
            WorldData data = WorldData.data((ServerWorld) (Object) this);
            if (data.blockEntityTickInvoker == null) {
                TickStage stage = ServerData.data(getServer()).getTickStageTree().peekLeaf();
                profiler.push("blockEntities");

                this.iteratingTickingBlockEntities = true;

                if (!this.pendingBlockEntityTickers.isEmpty()) {
                    this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
                    this.pendingBlockEntityTickers.clear();
                }

                for (BlockEntityTickInvoker ticker: this.blockEntityTickers) {
                    stage.getChildren().add(new BlockEntityStage((BlockEntitiesRootStage) stage, ticker));
                }
                this.pendingBlockEntityTickers.clear();

                // Reden debugger
                stage.yield();

                this.iteratingTickingBlockEntities = false;
                profiler.pop();
            }
            else {
                blockEntityTickInvoker = data.blockEntityTickInvoker;
                if (blockEntityTickInvoker.isRemoved()) {
                    var stage = (BlockEntityStage) ServerData.data(getServer()).getTickStageTree().peekLeaf();
                    this.blockEntityTickers.remove(stage.getTicker());
                } else if (this.shouldTickBlockPos(blockEntityTickInvoker.getPos())) {
                    blockEntityTickInvoker.tick();
                }
                data.blockEntityTickInvoker = null;
            }
            return;
        }

        profiler.push("blockEntities");

        this.iteratingTickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }

        iterator = this.blockEntityTickers.iterator();

        while(iterator.hasNext()) {
            blockEntityTickInvoker = iterator.next();
            if (blockEntityTickInvoker.isRemoved()) {
                iterator.remove();
            } else if (this.shouldTickBlockPos(blockEntityTickInvoker.getPos())) {
                blockEntityTickInvoker.tick();
            }
        }

        this.iteratingTickingBlockEntities = false;
        profiler.pop();
    }
}
