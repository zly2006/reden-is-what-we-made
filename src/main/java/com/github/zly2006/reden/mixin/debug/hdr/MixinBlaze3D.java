package com.github.zly2006.reden.mixin.debug.hdr;

import com.github.zly2006.reden.utils.FrameBufferUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = GlStateManager.class, remap = false)
public abstract class MixinBlaze3D {
    private static boolean hdrDebug = true;

    @ModifyArgs(
            method = "_texImage2D",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glTexImage2D(IIIIIIIILjava/nio/IntBuffer;)V"
            )
    )
    private static void modifyTexImage2DArgs(Args args) {
        System.out.println("internalFormat=" + args.<Integer>get(2) + ", format=" + args.<Integer>get(6) + ", type=" + args.<Integer>get(7));
        if (args.<Integer>get(2) == GL30.GL_RGBA || args.<Integer>get(2) == GL30.GL_RGBA8) {
            args.set(2, GL30.GL_RGBA16F);
        } else System.err.println("Unknown internalFormat: " + args.<Integer>get(2));
        if (args.<Integer>get(7) == GL30.GL_UNSIGNED_BYTE) {
            args.set(7, GL30.GL_FLOAT);
        } else System.err.println("Unknown type: " + args.<Integer>get(7));
        if (hdrDebug) {
            FrameBufferUtils.debug();
        }
    }
}
