package com.github.zly2006.reden.mixin.debugger.updater;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.WorldData;
import com.github.zly2006.reden.carpet.RedenCarpetSettings;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.block.NeighborUpdater;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Supplier;

@Mixin(value = ServerWorld.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServerWorld extends World implements WorldData.WorldDataAccess {
    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Redirect(
            method = "*",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/world/ServerWorld;neighborUpdater:Lnet/minecraft/world/block/NeighborUpdater;"
            )
    )
    private NeighborUpdater redirectNeighborUpdater(ServerWorld serverWorld) {
        if (RedenCarpetSettings.Options.redenDebuggerUpdater) {
            return getRedenWorldData().getRedenNeighborUpdater();
        } else {
            return serverWorld.neighborUpdater;
        }
    }
}
