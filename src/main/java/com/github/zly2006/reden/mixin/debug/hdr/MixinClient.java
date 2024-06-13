package com.github.zly2006.reden.mixin.debug.hdr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinClient {
    @Inject(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/mojang/bl aze3d/systems/RenderSystem;initRenderer(IZ)V")
    )
    private void beforeGlxInit(RunArgs args, CallbackInfo ci) {

    }
}
