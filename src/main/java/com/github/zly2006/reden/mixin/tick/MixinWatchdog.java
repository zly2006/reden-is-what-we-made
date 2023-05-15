package com.github.zly2006.reden.mixin.tick;

import com.github.zly2006.reden.debugger.FreezeKt;
import net.minecraft.server.dedicated.DedicatedServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DedicatedServerWatchdog.class)
public class MixinWatchdog {
    @ModifyVariable(method = "run", at = @At(value = "STORE", ordinal = 1), ordinal = 0)
    private long onTickTimeout(long time) {
        if (FreezeKt.getDisableWatchDog()) {
            return 0;
        } else {
            return time;
        }
    }
}
