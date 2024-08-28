package com.github.zly2006.reden.mixin.render;

import com.github.zly2006.reden.ImguiKt;
import com.github.zly2006.reden.ImguiScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, remap = false)
public class MixinRenderSystem {
    @Inject(
            method = "flipFrame",
            at = @At("HEAD")
    )
    private static void flipFrame(CallbackInfo ci) {
        // Call the function to render the imgui
        if (MinecraftClient.getInstance().currentScreen instanceof ImguiScreen || !ImguiKt.getHudRenderers().isEmpty()) {
            ImguiKt.renderFrame();
        }
    }
}
