package com.github.zly2006.reden.mixin.playerModel;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeadFeatureRenderer.class)
public abstract class ObserverHead<T extends LivingEntity, M extends EntityModel<T> & ModelWithHead> extends FeatureRenderer<T, M> {
    @Shadow
    @Final
    private float scaleX;

    @Shadow
    @Final
    private float scaleY;

    @Shadow
    @Final
    private float scaleZ;

    @Shadow
    @Final
    private HeldItemRenderer heldItemRenderer;

    ObserverHead() {
        super(null);
        RenderLayers.getBlockLayer(Blocks.STONE.getDefaultState()).getDrawMode();
    }

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD")
    )
    private void renderHead(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (livingEntity instanceof ClientPlayerEntity player) {
            matrixStack.push();
            matrixStack.scale(this.scaleX, this.scaleY, this.scaleZ);
            this.getContextModel().getHead().rotate(matrixStack);
            HeadFeatureRenderer.translate(matrixStack, false);
            matrixStack.translate(-0.5, -0.5, -0.5);
            RenderSystem.disableCull();
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            buffer.vertex(matrix, 0, 0, 0).color(0, 0, 0, 0);
            buffer.vertex(matrix, 1, 0, 0).color(0, 0, 0, 0);
            buffer.vertex(matrix, 1, 1, 0).color(0, 0, 0, 0);
            buffer.vertex(matrix, 0, 1, 0).color(0, 0, 0, 0);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            this.heldItemRenderer.renderItem(livingEntity, new ItemStack(Items.OBSERVER), ModelTransformationMode.HEAD, false, matrixStack, vertexConsumerProvider, light);
            matrixStack.pop();
        }
    }
}
