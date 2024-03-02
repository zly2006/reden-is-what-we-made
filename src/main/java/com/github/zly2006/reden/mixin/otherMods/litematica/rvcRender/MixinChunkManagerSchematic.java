package com.github.zly2006.reden.mixin.otherMods.litematica.rvcRender;

import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask;
import fi.dy.masa.litematica.world.ChunkManagerSchematic;
import fi.dy.masa.litematica.world.ChunkSchematic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkManagerSchematic.class, remap = false)
public abstract class MixinChunkManagerSchematic {
    @Shadow
    public abstract ChunkSchematic getChunk(int chunkX, int chunkZ);

    @Inject(
            method = "loadChunk",
            at = @At("RETURN")
    )
    private void addRvcToChunk(int chunkX, int chunkZ, CallbackInfo ci) {
        var task = RvcMoveStructureLitematicaTask.stackTop();
        if (task != null) {
            task.pasteChunk(getChunk(chunkX, chunkZ));
        }
    }
}
