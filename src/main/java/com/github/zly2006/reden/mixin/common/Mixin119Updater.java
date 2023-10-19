package com.github.zly2006.reden.mixin.common;


import com.github.zly2006.reden.access.UpdaterData;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ChainRestrictedNeighborUpdater.class)
public class Mixin119Updater implements UpdaterData.UpdaterDataAccess {
    @Unique private final UpdaterData updaterData = new UpdaterData();

    @NotNull
    @Override
    public UpdaterData getRedenUpdaterData() {
        return updaterData;
    }
}
