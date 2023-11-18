package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.access.WorldTickSchedulerAccess;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.world.ScheduledTicksRootStage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Mixin(value = WorldTickScheduler.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorldTickScheduler<T> implements TickStageOwnerAccess, WorldTickSchedulerAccess<T> {
    @Shadow @Final public Queue<OrderedTick<T>> tickableTicks;

    @Shadow @Final public Set<OrderedTick<?>> copiedTickableTicksList;

    @Shadow @Final public List<OrderedTick<T>> tickedTicks;

    @Shadow protected abstract void collectTickableTicks(long time, int maxTicks, Profiler profiler);

    @Shadow @Final private Supplier<Profiler> profilerGetter;

    @Shadow protected abstract void clear();

    @Unique ScheduledTicksRootStage stage;

    @Unique OrderedTick<T> tickingTick;

    @Nullable
    @Override
    public TickStage getTickStage() {
        return stage;
    }

    @Override
    public void setTickStage(@Nullable TickStage tickStage) {
        this.stage = (ScheduledTicksRootStage) tickStage;
    }

    @Override
    public void setTickingTick(OrderedTick<T> tickingTick) {
        this.tickingTick = tickingTick;
    }

    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public void tick(long time, int maxTicks, BiConsumer<BlockPos, T> ticker) {
        Profiler profiler = this.profilerGetter.get();
        profiler.push("collect");
        this.collectTickableTicks(time, maxTicks, profiler);

        for (OrderedTick<T> tick : tickableTicks) {
            stage.getChildren().add(stage.createChild(tick));
        }

        profiler.swap("run");
        profiler.visit("ticksToRun", this.tickableTicks.size());
        this.tick(ticker);
        profiler.swap("cleanup");
        this.clear();
        profiler.pop();
    }
    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public final void tick(BiConsumer<BlockPos, T> ticker) {
        OrderedTick<T> orderedTick = tickingTick; // leave local for capture
        if (tickingTick == null) {
            // first tome call, tick children
            stage.yield();
            return;
        }

        if (!this.copiedTickableTicksList.isEmpty()) {
            this.copiedTickableTicksList.remove(orderedTick);
        }

        this.tickedTicks.add(orderedTick);
        ticker.accept(orderedTick.pos(), orderedTick.type());
    }
}
