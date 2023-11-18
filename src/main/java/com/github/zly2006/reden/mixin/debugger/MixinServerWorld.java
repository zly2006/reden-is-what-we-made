package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.TickStageOwnerAccess;
import com.github.zly2006.reden.access.WorldData;
import com.github.zly2006.reden.debugger.TickStage;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import com.github.zly2006.reden.debugger.stages.world.*;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.spawner.Spawner;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.github.zly2006.reden.access.ServerData.data;

@Mixin(value = ServerWorld.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServerWorld extends World implements WorldData.WorldDataAccess {
    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow
    public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow
    private boolean inBlockTick;

    @Shadow
    protected abstract void tickWeather();

    @Shadow
    @Final
    private SleepManager sleepManager;

    @Shadow
    @Final
    private List<ServerPlayerEntity> players;

    @Shadow
    protected abstract void resetWeather();

    @Shadow
    public abstract void sendEntityDamage(Entity entity, DamageSource damageSource);

    @Shadow
    public abstract void setTimeOfDay(long timeOfDay);

    @Shadow
    protected abstract void wakeSleepingPlayers();

    @Shadow
    protected abstract void tickTime();

    @Shadow
    protected abstract void tickBlock(BlockPos pos, Block block);

    @Shadow
    protected abstract void tickFluid(BlockPos pos, Fluid fluid);

    @Shadow
    @Final
    private List<BlockEvent> blockEventQueue;

    @Shadow
    @Final
    private WorldTickScheduler<Block> blockTickScheduler;

    @Shadow
    @Final
    private WorldTickScheduler<Fluid> fluidTickScheduler;

    @Shadow
    @Final
    protected RaidManager raidManager;

    @Shadow
    public abstract LongSet getForcedChunks();

    @Shadow
    public abstract void resetIdleTimeout();

    @Shadow
    private int idleTimeout;

    @Shadow
    private @Nullable EnderDragonFight enderDragonFight;

    @Shadow
    @Final
    private EntityList entityList;

    @Shadow
    protected abstract boolean shouldCancelSpawn(Entity entity);

    @Shadow
    @Final
    private ServerChunkManager chunkManager;

    @Shadow
    @Final
    public ServerEntityManager<Entity> entityManager;

    @Shadow
    public abstract void tickEntity(Entity entity);

    @Shadow
    public abstract ServerChunkManager getChunkManager();

    @Shadow
    @Final
    private List<Spawner> spawners;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    public ObjectLinkedOpenHashSet<BlockEvent> syncedBlockEventQueue;

    @Shadow
    protected abstract boolean processBlockEvent(BlockEvent event);

    @Shadow
    public abstract void addSyncedBlockEvent(BlockPos pos, Block block, int type, int data);

    /**
     * @author zly2006
     * @reason Reden Debugger
     */
    @Overwrite
    public void tick(BooleanSupplier shouldKeepTicking) {
        Profiler profiler = this.getProfiler();
        TickStage stage = data(server).getTickStageTree().peekLeaf();
        if (stage instanceof WorldBorderStage) {
            this.inBlockTick = true;
            profiler.push("world border");
            this.getWorldBorder().tick();
        } else if (stage instanceof WeatherStage) {
            profiler.swap("weather");
            this.tickWeather();
        } else if (stage instanceof TimeStage) {
            int i = this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
            if (this.sleepManager.canSkipNight(i) && this.sleepManager.canResetTime(i, this.players)) {
                if (this.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                    long timeOfDay = this.properties.getTimeOfDay() + 24000L;
                    this.setTimeOfDay(timeOfDay - timeOfDay % 24000L);
                }

                this.wakeSleepingPlayers();
                if (this.getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE) && this.isRaining()) {
                    this.resetWeather();
                }
            }
            this.calculateAmbientDarkness();
            this.tickTime();
        } else if (stage instanceof BlockScheduledTicksRootStage) {
            profiler.swap("tickPending");
            if (!this.isDebugWorld()) {
                long time = this.getTime();
                profiler.push("blockTicks");
                // Reden start
                ((TickStageOwnerAccess) this.blockTickScheduler).setTickStage(stage);
                // Reden stop
                this.blockTickScheduler.tick(time, 65536, this::tickBlock);
                profiler.pop();
            }
        } else if (stage instanceof FluidScheduledTicksRootStage) {
            profiler.swap("tickPending");
            if (!this.isDebugWorld()) {
                long time = this.getTime();
                profiler.push("fluidTicks");
                // Reden start
                ((TickStageOwnerAccess) this.fluidTickScheduler).setTickStage(stage);
                // Reden stop
                this.fluidTickScheduler.tick(time, 65536, this::tickFluid);
                profiler.pop();
            }
        } else if (stage instanceof RaidStage) {
            profiler.swap("raid");
            this.raidManager.tick();
        } else if (stage instanceof RandomTickStage) {
            //todo
            profiler.swap("chunkSource");
            this.getChunkManager().tick(shouldKeepTicking, true);
        } else if (stage instanceof BlockEventsRootStage) {
            profiler.swap("blockEvents");
            // Reden start
            for (BlockEvent blockEvent : syncedBlockEventQueue) {
                stage.getChildren().add(new BlockEventStage((BlockEventsRootStage) stage, blockEvent));
            }
            syncedBlockEventQueue.clear();
            // Reden stop
            this.processSyncedBlockEvents();
            this.inBlockTick = false;
            profiler.pop();
        } else if (stage instanceof EntitiesRootStage) {
            boolean bl = !this.players.isEmpty() || !this.getForcedChunks().isEmpty();
            if (bl) {
                this.resetIdleTimeout();
            }
            if (bl || this.idleTimeout++ < 300) {
                profiler.push("entities");
                if (this.enderDragonFight != null) {
                    profiler.push("dragonFight");
                    this.enderDragonFight.tick();
                    profiler.pop();
                }

                this.entityList.forEach((entity) -> {
                    if (!entity.isRemoved()) {
                        if (this.shouldCancelSpawn(entity)) {
                            entity.discard();
                        } else {
                            profiler.push("checkDespawn");
                            entity.checkDespawn();
                            profiler.pop();
                            if (this.chunkManager.threadedAnvilChunkStorage.getTicketManager().shouldTickEntities(entity.getChunkPos().toLong())) {
                                Entity entity2 = entity.getVehicle();
                                if (entity2 != null) {
                                    if (!entity2.isRemoved() && entity2.hasPassenger(entity)) {
                                        return;
                                    }

                                    entity.stopRiding();
                                }

                                profiler.push("tick");
                                this.tickEntity(this::tickEntity, entity);
                                profiler.pop();
                            }
                        }
                    }
                });
                profiler.pop();
                this.tickBlockEntities();
            }
            profiler.push("entityManagement");
            this.entityManager.tick();
            profiler.pop();
        }
    }


    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public void tickSpawners(boolean spawnMonsters, boolean spawnAnimals) {
        // Vanilla local variables
        Iterator<?> iterator;
        Spawner spawner;
        // End

        WorldRootStage rootStage = getRedenWorldData().tickStage;
        spawner = rootStage.tickingSpawner;
        rootStage.tickingSpawner = null;
        if (spawner != null) {
            // The first call of this method is from ServerChunkManager.tick(), where spawner is null.
            spawner.spawn(rootStage.getWorld(), spawnMonsters, spawnAnimals);
        }
    }

    /**
     * @author zly2006
     * @reason Reden debugger
     */
    @Overwrite
    public final void processSyncedBlockEvents() {
        this.blockEventQueue.clear();
        BlockEvent blockEvent = getRedenWorldData().getTickingBlockEvent();

        if (blockEvent == null) {
            data(server).getTickStageTree().peekLeaf().yield();
            return;
        }

        if (this.shouldTickBlockPos(blockEvent.pos())) {
            if (this.processBlockEvent(blockEvent)) {
                this.server.getPlayerManager().sendToAround(null, blockEvent.pos().getX(), blockEvent.pos().getY(), blockEvent.pos().getZ(), 64.0, this.getRegistryKey(), new BlockEventS2CPacket(blockEvent.pos(), blockEvent.block(), blockEvent.type(), blockEvent.data()));
            }
        } else {
            this.blockEventQueue.add(blockEvent);
        }

        if (!syncedBlockEventQueue.isEmpty()) {
            Reden.LOGGER.error("Error: added new block event during processing.");
        }

        this.syncedBlockEventQueue.addAll(this.blockEventQueue);
    }
}
