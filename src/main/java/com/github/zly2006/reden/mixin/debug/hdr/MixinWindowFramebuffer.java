package com.github.zly2006.reden.mixin.debug.hdr;

import com.github.zly2006.reden.utils.FrameBufferUtils;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WindowFramebuffer.class)
public abstract class MixinWindowFramebuffer extends Framebuffer {
    public MixinWindowFramebuffer(boolean useDepth) {
        super(useDepth);
    }

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_bindTexture(I)V")
    )
    private void onInit(int width, int height, CallbackInfo ci) {
        System.out.println("@WindowFramebuffer.init(), depthAttachment: " + depthAttachment + ", colorAttachment: " + colorAttachment);
        FrameBufferUtils.debug();
    }
}
