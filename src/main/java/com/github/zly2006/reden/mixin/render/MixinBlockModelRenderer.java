package com.github.zly2006.reden.mixin.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    @Inject(
            method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
            at = @At("HEAD")
    )
    private void render$Head(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, Random random, long seed, int overlay, CallbackInfo ci) {
        /*
        var mc = MinecraftClient.getInstance();
        var outline = RenderLayer.getOutline(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        var outlineSource = mc.getBufferBuilders().getOutlineVertexConsumers();
        var blockModel = mc.getBlockRenderManager().getModel(state);
        mc.getBlockRenderManager().getModelRenderer().render(entry, outlineSource.getBuffer(outline), state, blockModel, 0f, 0f, 0f, 15, 0);
        */
    }
    @ModifyVariable(
            method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private VertexConsumer render$Head(VertexConsumer vertexConsumer) {
        return vertexConsumer;
    }
}
