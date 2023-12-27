package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    @ModifyArg(
            method = "addSyncedBlockEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;add(Ljava/lang/Object;)Z"
            )
    )
    private Object beforeAddSyncedBlockEvent(Object event) { // BlockEvent
        if (event instanceof UndoableAccess access) {
            PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
            if (recording != null) {
                access.setUndoId$reden(recording.getId());
            }
        }
        return event;
    }

    @Inject(
            method = "processBlockEvent",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/block/BlockState;onSyncedBlockEvent(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z"
            )
    )
    private void beforeProcessBlockEvent(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
        long undoId = ((UndoableAccess) event).getUndoId$reden();
        UpdateMonitorHelper.pushRecord(undoId, () -> "block event/" + event.pos().toShortString());
    }

    @Inject(
            method = "processBlockEvent",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/block/BlockState;onSyncedBlockEvent(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;II)Z"
            )
    )
    private void afterProcessBlockEvent(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
        UpdateMonitorHelper.popRecord(() -> "block event/" + event.pos().toShortString());
    }

    @Inject(
            method = "spawnEntity",
            at = @At("RETURN")
    )
    private void afterSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        UpdateMonitorHelper.isInitializingEntity = false;
    }
}
