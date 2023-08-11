package com.github.zly2006.reden.mixin.undo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
public class MixinSchedule {
    @Inject(
            method = "tick(Ljava/util/function/BiConsumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Queue;poll()Ljava/lang/Object;"
            )
    )
    private <T> void onRunSchedule(BiConsumer<BlockPos, T> ticker, CallbackInfo ci) {

    }
    @Inject(
            method = "scheduleTick",
            at = @At(
                    value = "HEAD"
            )
    )
    private <T> void onAddSchedule(OrderedTick<T> orderedTick, CallbackInfo ci) {

    }
}
