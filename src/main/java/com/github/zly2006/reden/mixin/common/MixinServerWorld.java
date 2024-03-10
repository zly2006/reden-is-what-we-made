package com.github.zly2006.reden.mixin.common;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.WorldData;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World implements WorldData.WorldDataAccess {
    @Unique
    WorldData worldData;
    @Unique
    int skippedUpdates = 0;

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState, CallbackInfo ci) {
        worldData = new WorldData((ServerWorld) (World) this);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @NotNull
    @Override
    public WorldData getRedenWorldData() {
        return worldData;
    }

    @Inject(method = "updateNeighbors", at = @At("HEAD"), cancellable = true)
    private void disableUpdates01(CallbackInfo ci) {
        if (worldData.updatesDisabled) {
            skippedUpdates++;
            ci.cancel();
        }
    }

    @Inject(method = "updateNeighbor*", at = @At("HEAD"), cancellable = true)
    private void disableUpdates02(CallbackInfo ci) {
        if (worldData.updatesDisabled) {
            skippedUpdates++;
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void beforeTick(CallbackInfo ci) {
        if (worldData.updatesDisabled) {
            Reden.LOGGER.warn("Did you forget to re-enable updates? Skipped " + skippedUpdates + " updates in " + getRegistryKey().getValue());
            skippedUpdates = 0;
            worldData.updatesDisabled = false;
        }
    }
}

