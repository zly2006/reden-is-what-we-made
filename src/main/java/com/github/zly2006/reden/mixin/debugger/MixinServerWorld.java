package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.WorldData;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import com.github.zly2006.reden.debugger.stages.world.*;
import com.github.zly2006.reden.debugger.tree.TickStageTree;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static com.github.zly2006.reden.access.ServerData.getData;

@Mixin(value = ServerWorld.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServerWorld extends World implements WorldData.WorldDataAccess {
    @Shadow
    @Final
    private MinecraftServer server;

    @Unique
    WorldRootStage worldRootStage;

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }


    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void redenTickBefore(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        final TickStageTree tree = getData(server).getTickStageTree();
        worldRootStage = new WorldRootStage(
                (ServerWorld) (Object) this,
                getData(server).getTickStage(),
                getRegistryKey().getValue()
        );
        tree.push$reden_is_what_we_made(worldRootStage);
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void redenTickAfter(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        final TickStageTree tree = getData(server).getTickStageTree();
        tree.pop(WorldRootStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;tick()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeWorldBorderTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new WorldBorderStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/border/WorldBorder;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterWorldBorderTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(WorldBorderStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickWeather()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeWeatherTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new WeatherStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickWeather()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterWeatherTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(WeatherStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickTime()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeTimeTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new TimeStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickTime()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterTimeTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(TimeStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeBlockScheduledTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new BlockScheduledTicksRootStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void afterBlockScheduledTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(BlockScheduledTicksRootStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeFluidScheduledTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new FluidScheduledTicksRootStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/tick/WorldTickScheduler;tick(JILjava/util/function/BiConsumer;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void afterFluidScheduledTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(FluidScheduledTicksRootStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/raid/RaidManager;tick()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeRaidTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new RaidStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/raid/RaidManager;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterRaidTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(RaidStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;tick(Ljava/util/function/BooleanSupplier;Z)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeChunkManagerTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new RandomTickStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;tick(Ljava/util/function/BooleanSupplier;Z)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterChunkManagerTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        getData(server).getTickStageTree().pop(RandomTickStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;processSyncedBlockEvents()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeBlockEventsTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new BlockEventsRootStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;processSyncedBlockEvents()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterBlockEventsTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(BlockEventsRootStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeEntitiesTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new EntitiesRootStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterEntitiesTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        getData(server).getTickStageTree().pop(EntitiesRootStage.class);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void beforeBlockEntitiesTick(CallbackInfo ci) {
        getData(server).getTickStageTree().push$reden_is_what_we_made(new BlockEntitiesRootStage(worldRootStage));
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterBlockEntitiesTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(BlockEntitiesRootStage.class);
    }

    @Inject(
            method = "tickBlock",
            at = @At("HEAD")
    )
    private void beforeBlockTick(BlockPos pos, Block block, CallbackInfo ci) {
        var parent = (BlockScheduledTicksRootStage) getData(server).getTickStageTree().getActiveStage();
        getData(server).getTickStageTree().push$reden_is_what_we_made(new BlockScheduledTickStage(parent, pos, block));
    }

    @Inject(
            method = "tickBlock",
            at = @At("RETURN")
    )
    private void afterBlockTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(BlockScheduledTickStage.class);
    }

    @Inject(
            method = "tickFluid",
            at = @At("HEAD")
    )
    private void beforeFluidTick(BlockPos pos, Fluid fluid, CallbackInfo ci) {
        var parent = (FluidScheduledTicksRootStage) getData(server).getTickStageTree().getActiveStage();
        getData(server).getTickStageTree().push$reden_is_what_we_made(new FluidScheduledTickStage(parent, pos, fluid));
    }

    @Inject(
            method = "tickFluid",
            at = @At("RETURN")
    )
    private void afterFluidTick(CallbackInfo ci) {
        getData(server).getTickStageTree().pop(FluidScheduledTickStage.class);
    }
}
