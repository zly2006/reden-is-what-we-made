package com.github.zly2006.reden.mixin.render;

import com.github.zly2006.reden.malilib.MalilibSettingsKt;
import com.github.zly2006.reden.render.BlockOutline;
import com.github.zly2006.reden.render.SolidFaceRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Unique
    SolidFaceRenderer solidFaceRenderer = new SolidFaceRenderer();

    @Inject(method = "render", at = @At("HEAD"))
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {

    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        this.solidFaceRenderer.tick();
    }

    @Redirect(
            method = "drawEntityOutlinesFramebuffer",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;blendFuncSeparate(Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;Lcom/mojang/blaze3d/platform/GlStateManager$SrcFactor;Lcom/mojang/blaze3d/platform/GlStateManager$DstFactor;)V",
                    ordinal = 0
            )
    )
    private void debug(GlStateManager.SrcFactor srcFactor, GlStateManager.DstFactor dstFactor, GlStateManager.SrcFactor srcAlpha, GlStateManager.DstFactor dstAlpha) {
        if (!MalilibSettingsKt.ENTITY_OUTLINE_RENDER_RAW.getBooleanValue()) {
            RenderSystem.blendFuncSeparate(srcFactor, dstFactor, srcAlpha, dstAlpha);
        }
    }

    @SuppressWarnings({"InvalidInjectorMethodSignature", "MixinAnnotationTarget"}) // Mcdev ????
    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "LOAD",
                    ordinal = 0
            ),
            ordinal = 3
    )
    private boolean forceOutline(boolean x) {
        return x || !BlockOutline.INSTANCE.getBlocks().isEmpty();
    }

    @SuppressWarnings("deprecation")
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void onRenderOutline(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (BlockOutline.INSTANCE.getBlocks().isEmpty()) return;
        OutlineVertexConsumerProvider vertexConsumers = this.bufferBuilders.getOutlineVertexConsumers();
        int color = BlockOutline.INSTANCE.getColor();
        vertexConsumers.setColor(color >> 16 & 255, color >> 8 & 255, color & 255, 255);
        // only render to outline frame buffer
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getOutline(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        RenderSystem.disableCull();
        BlockOutline.INSTANCE.render(buffer, matrices, camera, vertexConsumers);
        RenderSystem.enableCull();
    }
}
