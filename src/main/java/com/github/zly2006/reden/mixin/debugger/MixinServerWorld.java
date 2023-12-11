package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.WorldData;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.EntityList;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.spawner.SpecialSpawner;
import net.minecraft.world.tick.TickManager;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Supplier;

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
    private List<SpecialSpawner> spawners;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    public ObjectLinkedOpenHashSet<BlockEvent> syncedBlockEventQueue;

    @Shadow
    protected abstract boolean processBlockEvent(BlockEvent event);

    @Shadow protected abstract void method_31420(TickManager par1, Profiler par2, Entity par3);
}
