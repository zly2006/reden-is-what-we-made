package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.access.UndoableAccess;
import com.github.zly2006.reden.mixinhelper.UpdateMonitorHelper;
import com.github.zly2006.reden.utils.DebugKt;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld {
    @Shadow public abstract void removePlayer(ServerPlayerEntity player, Entity.RemovalReason reason);

    @Redirect(
            method = "addSyncedBlockEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;add(Ljava/lang/Object;)Z",
                    remap = false
            )
    )
    private boolean onAddBlockEvent(ObjectLinkedOpenHashSet<BlockEvent> instance, Object curr) {
        if (curr instanceof UndoableAccess access) {
            PlayerData.UndoRecord recording = UpdateMonitorHelper.INSTANCE.getRecording();
            if (recording != null) {
                access.setUndoId(recording.getId());
            }
        }
        return instance.add((BlockEvent) curr);
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
        long undoId = ((UndoableAccess) event).getUndoId();
        DebugKt.debugLogger.invoke("block event start at " + event.pos().toShortString() + event.block().toString() + ", data=" + event.data() + ", type=" + event.type() + ", record "+ undoId);
        UpdateMonitorHelper.pushRecord(undoId, "block event");
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
        DebugKt.debugLogger.invoke("block event end");
        UpdateMonitorHelper.popRecord("block event");
    }
}
