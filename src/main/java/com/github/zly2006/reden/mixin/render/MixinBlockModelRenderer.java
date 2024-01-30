package com.github.zly2006.reden.mixin.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    @ModifyVariable(
            method = "render(Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;JI)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private VertexConsumer render$Head(VertexConsumer vertexConsumer) {
//        return VertexConsumers.union(vertexConsumer, new BufferBuilder(10000));
        return vertexConsumer;
    }
}
