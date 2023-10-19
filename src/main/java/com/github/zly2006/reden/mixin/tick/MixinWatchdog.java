package com.github.zly2006.reden.mixin.tick;

import com.github.zly2006.reden.debugger.FreezeKt;
import net.minecraft.server.dedicated.DedicatedServerWatchdog;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DedicatedServerWatchdog.class)
public class MixinWatchdog {
    @Redirect(
            method = "run",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;getMeasuringTimeMs()J"
            )
    )
    private long onTickTimeout() {
        if (FreezeKt.getDisableWatchDog()) {
            return 0;
        } else {
            return Util.getMeasuringTimeMs();
        }
    }
}
