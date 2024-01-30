package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WorldTickScheduler.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorldTickScheduler<T> {
    @Shadow
    @Final
    private Long2ObjectMap<ChunkTickScheduler<T>> chunkTickSchedulers;
}
