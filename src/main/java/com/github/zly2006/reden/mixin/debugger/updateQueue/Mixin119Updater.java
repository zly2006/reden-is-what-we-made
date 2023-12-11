package com.github.zly2006.reden.mixin.debugger.updateQueue;


import com.github.zly2006.reden.Reden;
import net.minecraft.world.World;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater.Entry;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayDeque;
import java.util.List;

@Mixin(value = ChainRestrictedNeighborUpdater.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class Mixin119Updater implements NeighborUpdater {
    @Shadow @Final private World world;

    @Shadow @Final private ArrayDeque<Entry> queue;

    @Shadow @Final private List<Entry> pending;
}
