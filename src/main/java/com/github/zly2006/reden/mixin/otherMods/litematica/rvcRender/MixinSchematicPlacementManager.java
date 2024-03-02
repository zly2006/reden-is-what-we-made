package com.github.zly2006.reden.mixin.otherMods.litematica.rvcRender;

import com.github.zly2006.reden.rvc.gui.hud.gameplay.RvcMoveStructureLitematicaTask;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SchematicPlacementManager.class, remap = false)
public class MixinSchematicPlacementManager {
    @Unique
    static private final boolean ignoreLitematicaTaskTime =
            System.getProperty("reden.ignoreLitematicaTaskTime", "false").equals("true");

    @ModifyExpressionValue(
            method = "processQueuedChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ArrayListMultimap;containsKey(Ljava/lang/Object;)Z"
            )
    )
    private boolean dontSkipReden001(boolean original, @Local ChunkPos pos) {
        RvcMoveStructureLitematicaTask litematicaTask = RvcMoveStructureLitematicaTask.stackTop();
        if (litematicaTask != null && litematicaTask.previewContainsChunk(pos)) {
            return true;
        } else {
            return original;
        }
    }

    @ModifyExpressionValue(
            method = "processQueuedChunks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collection;isEmpty()Z"
            )
    )
    private boolean dontSkipReden002(boolean original, @Local ChunkPos pos) {
        RvcMoveStructureLitematicaTask litematicaTask = RvcMoveStructureLitematicaTask.stackTop();
        if (litematicaTask != null && litematicaTask.previewContainsChunk(pos)) {
            return false;
        } else {
            return original;
        }
    }

    @Inject(
            method = "hasTimeToExecuteMoreTasks",
            at = @At("RETURN"),
            cancellable = true
    )
    private void dontMindTheTime(CallbackInfoReturnable<Boolean> cir) {
        if (ignoreLitematicaTaskTime) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "processQueuedChunks",
            at = @At(value = "INVOKE", target = "Lfi/dy/masa/litematica/world/WorldSchematic;scheduleChunkRenders(II)V")
    )
    private void pasteRedenBeforeRender(CallbackInfo ci, @Local ChunkPos pos, @Local WorldSchematic worldSchematic) {
        var task = RvcMoveStructureLitematicaTask.stackTop();
        if (task != null) {
            task.pasteChunk(worldSchematic.getChunk(pos.x, pos.z));
        }
    }
}
