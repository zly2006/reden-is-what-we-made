package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.debugger.stages.WorldRootStage;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}
