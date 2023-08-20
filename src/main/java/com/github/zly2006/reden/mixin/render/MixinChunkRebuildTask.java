package com.github.zly2006.reden.mixin.render;

import com.github.zly2006.reden.render.StructureOutline;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Set;

@Mixin(ChunkBuilder.BuiltChunk.RebuildTask.class)
public class MixinChunkRebuildTask {
    private BlockPos pos;
    private BlockState state;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void capturePos(float cameraX, float cameraY, float cameraZ, BlockBufferBuilderStorage storage, CallbackInfoReturnable<ChunkBuilder.BuiltChunk.RebuildTask.RenderData> cir, ChunkBuilder.BuiltChunk.RebuildTask.RenderData renderData, int i, BlockPos blockPos, BlockPos blockPos2, ChunkOcclusionDataBuilder chunkOcclusionDataBuilder, ChunkRendererRegion chunkRendererRegion, MatrixStack matrixStack, Set set, Random random, BlockRenderManager blockRenderManager, Iterator var15, BlockPos blockPos3, BlockState blockState, BlockState blockState2, FluidState fluidState, RenderLayer renderLayer, BufferBuilder bufferBuilder) {
        pos = blockPos3;
        state = blockState;



        boolean hasOutline = pos != null && StructureOutline.INSTANCE.getSet$reden_is_what_we_made().contains(pos);

        if (true) return;
        if (hasOutline || true) {
            /*
            var buffers = MinecraftClient.getInstance().getBufferBuilders();
            if (buffers.getBlockBufferBuilders().get(RenderLayer.getSolid()) == vertexConsumer) {
                return buffers.getOutlineVertexConsumers().getBuffer(RenderLayer.getSolid());
            }
            */
            var mc = MinecraftClient.getInstance();
            var outline = RenderLayer.getOutline(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            var outlineSource = mc.getBufferBuilders().getOutlineVertexConsumers();
            var blockModel = mc.getBlockRenderManager().getModel(state);
            mc.getBlockRenderManager().getModelRenderer().render(matrixStack.peek(), outlineSource.getBuffer(outline), state, blockModel, 0f, 0f, 0f, 15, 0);
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/BlockRenderManager;renderBlock(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLnet/minecraft/util/math/random/Random;)V"
            )
    )
    private VertexConsumer render(VertexConsumer vertexConsumer) {

        return vertexConsumer;
    }
}
