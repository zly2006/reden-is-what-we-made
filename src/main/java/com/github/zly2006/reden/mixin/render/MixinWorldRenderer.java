package com.github.zly2006.reden.mixin.render;

import com.github.zly2006.reden.render.SolidFaceRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;
    @Shadow private @Nullable Framebuffer entityOutlinesFramebuffer;
    @Shadow @Final private MinecraftClient client;
    @Unique
    SolidFaceRenderer solidFaceRenderer = new SolidFaceRenderer();

    @Inject(method = "render", at = @At("HEAD"))
    private void render(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {

    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        this.solidFaceRenderer.tick();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/world/ClientWorld;getEntities()Ljava/lang/Iterable;",
                    ordinal = 0
            )
    )
    private void s(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
        VertexConsumer buffer = bufferBuilders.getOutlineVertexConsumers().getBuffer(RenderLayer.getDebugQuads());
        bufferBuilders.getOutlineVertexConsumers().setColor(0, 255, 0, 255);
        var x = 10 - camera.getPos().x;
        var y = 10 - camera.getPos().y;
        var z = 10 - camera.getPos().z;
        buffer.vertex(x, y, z).color(1, 1, 1, 1).next();
        buffer.vertex(x, y, z + 1).color(1, 1, 1, 1).next();
        buffer.vertex(x + 1, y, z + 1).color(1, 1, 1, 1).next();
        buffer.vertex(x + 1, y, z).color(1, 1, 1, 1).next();
        bufferBuilders.getOutlineVertexConsumers().draw();
        bufferBuilders.getEntityVertexConsumers().draw();
        entityOutlinesFramebuffer.draw(client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight(), false);
    }
}
