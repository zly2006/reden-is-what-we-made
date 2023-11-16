package com.github.zly2006.reden.access

import net.minecraft.world.tick.OrderedTick

interface WorldTickSchedulerAccess<T> {
    fun setTickingTick(tickingTick: OrderedTick<T>)
}
