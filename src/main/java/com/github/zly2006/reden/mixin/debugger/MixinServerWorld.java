package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.WorldData;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import com.github.zly2006.reden.debugger.stages.world.*;
import com.github.zly2006.reden.debugger.tree.TickStageTree;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
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

import static com.github.zly2006.reden.access.ServerData.data;

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
        final TickStageTree tree = data(server).getTickStageTree();
        worldRootStage = new WorldRootStage((ServerWorld) (Object) this, data(server).getTickStage(), shouldKeepTicking);
        tree.push$reden_is_what_we_made(worldRootStage);
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void redenTickAfter(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        final TickStageTree tree = data(server).getTickStageTree();
        tree.pop$reden_is_what_we_made();
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
        data(server).getTickStageTree().push$reden_is_what_we_made(new WorldBorderStage(worldRootStage));
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
        data(server).getTickStageTree().pop$reden_is_what_we_made();
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
        data(server).getTickStageTree().push$reden_is_what_we_made(new WeatherStage(worldRootStage));
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
        data(server).getTickStageTree().pop$reden_is_what_we_made();
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
        data(server).getTickStageTree().push$reden_is_what_we_made(new TimeStage(worldRootStage));
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
        data(server).getTickStageTree().pop$reden_is_what_we_made();
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
        data(server).getTickStageTree().push$reden_is_what_we_made(new BlockScheduledTicksRootStage(worldRootStage));
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
        data(server).getTickStageTree().pop$reden_is_what_we_made();
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
        data(server).getTickStageTree().push$reden_is_what_we_made(new FluidScheduledTicksRootStage(worldRootStage));
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
        data(server).getTickStageTree().pop$reden_is_what_we_made();
    }
}
