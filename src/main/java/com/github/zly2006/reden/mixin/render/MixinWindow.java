package com.github.zly2006.reden.mixin.render;

import com.github.zly2006.reden.ImguiKt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinWindow {
    @Shadow
    @Final
    private Window window;

    @Inject(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(IZ)V", remap = false)
    )
    private void postInit(CallbackInfo ci) {
        System.setProperty("org.lwjgl.util.NoChecks", "true");
        ImguiKt.initImgui(this.window.getHandle());
    }
}
