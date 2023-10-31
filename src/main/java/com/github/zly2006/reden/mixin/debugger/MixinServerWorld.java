package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.WorldData;
import com.github.zly2006.reden.exceptions.RedenCoroutineCriticalError;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(value = ServerWorld.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServerWorld extends World implements WorldData.WorldDataAccess {
    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Shadow public abstract List<ServerPlayerEntity> getPlayers();

    @Shadow public abstract PortalForcer getPortalForcer();

    @Shadow private boolean inBlockTick;

    @Shadow protected abstract void tickWeather();

    @Shadow @Final private SleepManager sleepManager;

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow protected abstract void resetWeather();

    @Shadow public abstract void sendEntityDamage(Entity entity, DamageSource damageSource);

    @Shadow public abstract void setTimeOfDay(long timeOfDay);

    @Shadow protected abstract void wakeSleepingPlayers();

    @Shadow protected abstract void tickTime();

    @Shadow protected abstract void tickBlock(BlockPos pos, Block block);

    @Shadow protected abstract void tickFluid(BlockPos pos, Fluid fluid);

    @Shadow @Final private List<BlockEvent> blockEventQueue;

    @Shadow @Final private WorldTickScheduler<Block> blockTickScheduler;

    @Shadow @Final private WorldTickScheduler<Fluid> fluidTickScheduler;

    @Shadow @Final protected RaidManager raidManager;

    @Shadow protected abstract void processSyncedBlockEvents();

    @Shadow public abstract LongSet getForcedChunks();

    @Shadow public abstract void resetIdleTimeout();

    @Shadow private int idleTimeout;

    @Shadow private @Nullable EnderDragonFight enderDragonFight;

    @Shadow @Final private EntityList entityList;

    @Shadow protected abstract boolean shouldCancelSpawn(Entity entity);

    @Shadow @Final private ServerChunkManager chunkManager;

    @Shadow @Final public ServerEntityManager<Entity> entityManager;

    @Shadow public abstract void tickEntity(Entity entity);

    @Shadow public abstract ServerChunkManager getChunkManager();

    /**
     * @author zly2006
     * @reason Reden Debugger
     */
    @Overwrite
    public void tick(BooleanSupplier shouldKeepTicking) {
        Profiler profiler = this.getProfiler();
        int label = getRedenWorldData().tickStage.getTickLabel();
        switch (label) {
            case 0 -> {
                this.inBlockTick = true;
                profiler.push("world border");
                this.getWorldBorder().tick();
                profiler.swap("weather");
                this.tickWeather();

                getRedenWorldData().tickStage.setTickLabel(1);
                getRedenWorldData().tickStage.yieldAndTick();
            }
            case 1 -> {
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

                getRedenWorldData().tickStage.setTickLabel(2);
                getRedenWorldData().tickStage.yieldAndTick();
            }
            case 2 -> {
                profiler.swap("tickPending");
                if (!this.isDebugWorld()) {
                    long time = this.getTime();
                    profiler.push("blockTicks");
                    this.blockTickScheduler.tick(time, 65536, this::tickBlock);
                    profiler.swap("fluidTicks");
                    this.fluidTickScheduler.tick(time, 65536, this::tickFluid);
                    profiler.pop();
                }

                getRedenWorldData().tickStage.setTickLabel(3);
                getRedenWorldData().tickStage.yieldAndTick();
            }
            case 3 -> {
                profiler.swap("raid");
                this.raidManager.tick();

                getRedenWorldData().tickStage.setTickLabel(4);
                getRedenWorldData().tickStage.yieldAndTick();
            }
            case 4 -> {
                profiler.swap("chunkSource");
                this.getChunkManager().tick(shouldKeepTicking, true);
                profiler.swap("blockEvents");
                this.processSyncedBlockEvents();
                this.inBlockTick = false;
                profiler.pop();
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
            default -> throw new RedenCoroutineCriticalError("ServerWorld, label = " + label);
        }
    }
}
