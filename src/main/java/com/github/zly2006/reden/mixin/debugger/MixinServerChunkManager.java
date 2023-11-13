package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import com.google.common.collect.Lists;
import net.minecraft.server.world.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.GameRules;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.github.zly2006.reden.access.WorldData.data;

@Mixin(value = ServerChunkManager.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServerChunkManager {
    @Shadow @Final ServerWorld world;

    @Shadow private long lastMobSpawningTime;

    @Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

    @Shadow @Final public ChunkTicketManager ticketManager;

    @Shadow @Nullable private SpawnHelper.@Nullable Info spawnInfo;

    @Shadow protected abstract void ifChunkLoaded(long pos, Consumer<WorldChunk> chunkConsumer);

    @Shadow private boolean spawnMonsters;

    @Shadow private boolean spawnAnimals;

    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;Z)V",
            at = @At("HEAD")
    )
    private void onTick(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
    }

    @Unique WorldRootStage getWorldRootStage() {
        return data(world).tickStage;
    }

    /**
     * @author zly2006
     * @reason Reden Debugger
     */
    @Overwrite
    private void tickChunks() {
        long l = this.world.getTime();
        long m = l - this.lastMobSpawningTime;
        this.lastMobSpawningTime = l;
        boolean bl = this.world.isDebugWorld();
        if (bl) {
            this.threadedAnvilChunkStorage.tickEntityMovement();
        } else {
            WorldProperties worldProperties = this.world.getLevelProperties();
            Profiler profiler = this.world.getProfiler();
            profiler.push("pollingChunks");
            int i = this.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            boolean bl2 = worldProperties.getTime() % 400L == 0L;
            profiler.push("naturalSpawnCount");
            int j = this.ticketManager.getTickedChunkCount();
            SpawnHelper.Info info = SpawnHelper.setupSpawn(j, this.world.iterateEntities(), this::ifChunkLoaded, new SpawnDensityCapper(this.threadedAnvilChunkStorage));
            this.spawnInfo = info;
            profiler.swap("filteringLoadedChunks");
            List<ServerChunkManager.ChunkWithHolder> list = Lists.newArrayListWithCapacity(j);

            for (ChunkHolder chunkHolder : this.threadedAnvilChunkStorage.entryIterator()) {
                WorldChunk worldChunk = chunkHolder.getWorldChunk();
                if (worldChunk != null) {
                    list.add(new ServerChunkManager.ChunkWithHolder(worldChunk, chunkHolder));
                }
            }

            profiler.swap("spawnAndTick");
            boolean bl3 = this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
            Collections.shuffle(list);

            for (ServerChunkManager.ChunkWithHolder chunkWithHolder : list) {
                WorldChunk worldChunk2 = chunkWithHolder.chunk;
                ChunkPos chunkPos = worldChunk2.getPos();
                if (this.world.shouldTick(chunkPos) && this.threadedAnvilChunkStorage.shouldTick(chunkPos)) {
                    worldChunk2.increaseInhabitedTime(m);
                    if (bl3 && (this.spawnMonsters || this.spawnAnimals) && this.world.getWorldBorder().contains(chunkPos)) {
                        SpawnHelper.spawn(this.world, worldChunk2, info, this.spawnAnimals, this.spawnMonsters, bl2);
                    }

                    if (this.world.shouldTickBlocksInChunk(chunkPos.toLong())) {
                        this.world.tickChunk(worldChunk2, i);
                    }
                }
            }

            profiler.swap("customSpawners");
            if (bl3) {
                this.world.tickSpawners(this.spawnMonsters, this.spawnAnimals);
            }

            profiler.swap("broadcast");
            list.forEach((chunk) -> {
                chunk.holder.flushUpdates(chunk.chunk);
            });
            profiler.pop();
            profiler.pop();
            this.threadedAnvilChunkStorage.tickEntityMovement();
        }
    }
}
