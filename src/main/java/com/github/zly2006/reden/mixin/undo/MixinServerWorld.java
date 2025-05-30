package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockEventData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class MixinServerWorld {
    @ModifyArg(
            method = "blockEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private Object beforeAddSyncedBlockEvent(Object event) { // BlockEvent
        if (event instanceof UndoableAccess access) {
            PlayerData.UndoRecord recording = UndoMixinHelper.INSTANCE.getRecording();
            if (recording != null) {
                access.setUndoId$reden(recording.getId());
            }
        }
        return event;
    }

    @Inject(
            method = "doBlockEvent",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/world/level/block/state/BlockState;triggerEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z"
            )
    )
    private void beforeProcessBlockEvent(BlockEventData event, CallbackInfoReturnable<Boolean> cir) {
        long undoId = ((UndoableAccess) event).getUndoId$reden();
        UndoMixinHelper.pushRecord(undoId, () -> "block event/" + event.pos().toShortString());
    }

    @Inject(
            method = "doBlockEvent",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/block/state/BlockState;triggerEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z"
            )
    )
    private void afterProcessBlockEvent(BlockEventData event, CallbackInfoReturnable<Boolean> cir) {
        UndoMixinHelper.popRecord(() -> "block event/" + event.pos().toShortString());
    }

    @Inject(
            method = "addEntity",
            at = @At("RETURN")
    )
    private void afterSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        UndoMixinHelper.isInitializingEntity = false;
    }
}
