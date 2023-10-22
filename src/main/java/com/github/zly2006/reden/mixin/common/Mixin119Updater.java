package com.github.zly2006.reden.mixin.common;


import com.github.zly2006.reden.access.UpdaterData;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ChainRestrictedNeighborUpdater.class)
public abstract class Mixin119Updater implements UpdaterData.UpdaterDataAccess, NeighborUpdater {

}
