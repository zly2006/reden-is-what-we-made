package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.access.WorldTickSchedulerAccess;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.world.ScheduledTicksRootStage;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Queue;
import java.util.Set;
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

    @Nullable
    public TickStage getTickStage$reden() {
        return stage;
    }

    public void setTickStage$reden(@Nullable TickStage tickStage) {
        this.stage = (ScheduledTicksRootStage) tickStage;
    }
}
