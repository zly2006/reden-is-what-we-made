package com.github.zly2006.reden.mixin.undo;

import com.github.zly2006.reden.access.PlayerData;
import com.github.zly2006.reden.mixinhelper.UndoMixinHelper;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinPlayer {
    @Unique
    private ServerPlayer self() {
        return (ServerPlayer) (Object) this;
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void onAttack(CallbackInfo ci) {
        if (1 == 1) {
            UndoMixinHelper.playerStartRecording(self(), PlayerData.UndoRecord.Cause.ATTACK_ENTITY);
        }
    }
    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;attack(Lnet/minecraft/world/entity/Entity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void afterAttack(CallbackInfo ci) {
        if (1 == 1) {
            UndoMixinHelper.playerStopRecording(self());
        }
    }
}
