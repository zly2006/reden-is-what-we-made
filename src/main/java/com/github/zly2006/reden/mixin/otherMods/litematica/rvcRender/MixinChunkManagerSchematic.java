package com.github.zly2006.reden.mixin.otherMods.litematica.rvcRender;

import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask;
import com.github.zly2006.reden.task.Task;
import com.github.zly2006.reden.task.TaskKt;
import fi.dy.masa.litematica.world.ChunkManagerSchematic;
import fi.dy.masa.litematica.world.ChunkSchematic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ChunkManagerSchematic.class, remap = false)
public abstract class MixinChunkManagerSchematic {
    @Shadow
    public abstract ChunkSchematic getChunk(int chunkX, int chunkZ);

    @Inject(
            method = "loadChunk",
            at = @At("RETURN")
    )
    private void addRvcToChunk(int chunkX, int chunkZ, CallbackInfo ci) {
        List<Task> taskStack = TaskKt.getTaskStack();
        if (taskStack.isEmpty()) return;
        Task task = taskStack.get(taskStack.size() - 1);
        if (task instanceof RvcMoveStructureLitematicaTask litematicaTask) {
            // todo: pasteChunk(getChunk(chunkX, chunkZ))
            litematicaTask.setCurrentOrigin(litematicaTask.getCurrentOrigin());
        }
    }
}
