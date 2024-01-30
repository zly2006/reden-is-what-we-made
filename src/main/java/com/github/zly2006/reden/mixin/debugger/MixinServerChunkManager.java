package com.github.zly2006.reden.mixin.debugger;

import com.github.zly2006.reden.Reden;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(value = ServerChunkManager.class, priority = Reden.REDEN_HIGHEST_MIXIN_PRIORITY)
public abstract class MixinServerChunkManager {
    @Inject(
            method = "tick(Ljava/util/function/BooleanSupplier;Z)V",
            at = @At("HEAD")
    )
    private void onTick(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
    }
}
