package com.github.zly2006.reden.mixin.debugger.schedule;

import com.github.zly2006.reden.Reden;
import com.github.zly2006.reden.access.ServerData;
import com.github.zly2006.reden.utils.UtilsKt;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.zly2006.reden.access.ServerData.getData;

@Mixin(value = WorldTickScheduler.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinWorldTickScheduler<T> {
    @Shadow protected abstract void clear();

    @Inject(
            method = "scheduleTick",
            at = @At("HEAD")
    )
    private void onTickScheduled(OrderedTick<T> orderedTick, CallbackInfo ci) {
        ServerData data = getData(UtilsKt.getServer());
        data.getTickStageTree().onTickScheduled(orderedTick);
    }
}
