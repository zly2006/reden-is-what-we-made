package com.github.zly2006.reden.access;

import net.minecraft.world.tick.OrderedTick;

public interface WorldTickSchedulerAccess<T> {
    void setTickingTick(OrderedTick<T> tickingTick);
}
