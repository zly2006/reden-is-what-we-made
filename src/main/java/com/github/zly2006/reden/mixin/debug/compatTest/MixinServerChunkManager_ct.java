package com.github.zly2006.reden.mixin.debug.compatTest;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager_ct {
    @Inject(
            method = "tickChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SpawnHelper;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/SpawnHelper$Info;ZZZ)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void localCaptureTest(CallbackInfo ci, long l, long m, boolean bl, WorldProperties worldProperties, Profiler profiler, int i, boolean bl2, int j, SpawnHelper.Info info, List list, boolean bl3, Iterator var14, ServerChunkManager.ChunkWithHolder chunkWithHolder, WorldChunk worldChunk2, ChunkPos chunkPos) {

    }
}
