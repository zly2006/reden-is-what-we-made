package com.github.zly2006.reden.mixin.otherMods.litematica.rvcRender;

import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SchematicPlacementManager.class, remap = false)
public class MixinSchematicPlacementManager {
    @Inject(
            method = "processQueuedChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"
            )
    )
    private void dontSkipReden(CallbackInfo ci, @Local ChunkPos pos, @Local WorldSchematic worldSchematic) {
        RvcMoveStructureLitematicaTask litematicaTask = RvcMoveStructureLitematicaTask.stackTop();
        if (litematicaTask != null && litematicaTask.previewContainsChunk(pos)) {
            worldSchematic.scheduleChunkRenders(pos.x, pos.z);
        }
    }
}
