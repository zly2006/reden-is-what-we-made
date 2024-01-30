package com.github.zly2006.reden.render

import com.github.zly2006.reden.malilib.FANCY_BLOCK_OUTLINE
import io.netty.util.concurrent.CompleteFuture
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.BufferBuilder.BuiltBuffer
import net.minecraft.client.render.chunk.BlockBufferBuilderStorage
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random

@Environment(EnvType.CLIENT)
object BlockOutline {
    fun prepareUploadForLayer(futureList: MutableList<CompleteFuture<Void>>) {

    }

    /**
     * From: [net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk.RebuildTask.render]
     *
     * todo: [net.minecraft.client.render.chunk.ChunkBuilder.scheduleUpload]
     *
     * todo: bufferStorage, see vanilla, is that the same builders as immediate?
     */
    fun render(bufferStorage: BlockBufferBuilderStorage) {
        val mc = MinecraftClient.getInstance()
        val nonNullLayers = mutableSetOf<RenderLayer>()
        val blockRenderManager = mc.blockRenderManager

        val uploadFutures = mutableListOf<CompleteFuture<Void>>()
        val matrixStack = MatrixStack()
        val random = Random.create()
        blocks.forEach { (pos, state) ->
            if (!state.fluidState.isEmpty) {
                val renderLayer = RenderLayers.getFluidLayer(state.fluidState)
                val builder = bufferStorage.get(renderLayer)
                if (nonNullLayers.add(renderLayer)) {
                    prepareUploadForLayer(uploadFutures)
                }
                // we dont have the chunkRendererRegion, so we use mc.world
                blockRenderManager.renderFluid(pos, mc.world, builder, state, state.fluidState)
            }
            if (state.renderType == BlockRenderType.INVISIBLE) return@forEach
            val renderLayer = RenderLayers.getBlockLayer(state)
            val bufferBuilder = bufferStorage.get(renderLayer)
            if (nonNullLayers.add(renderLayer)) {
                prepareUploadForLayer(uploadFutures)
            }
            matrixStack.push()
            matrixStack.translate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            blockRenderManager.renderBlock(
                state,
                pos,
                mc.world,
                matrixStack,
                bufferBuilder,
                true,
                random
            )
            matrixStack.pop()
        }
// Vanilla:
//    we are rendering outline, so sorting doesn't matter
//
//        if (set.contains(RenderLayer.getTranslucent()) && !(bufferBuilder2 = storage.get(RenderLayer.getTranslucent())).isBatchEmpty()) {
//            bufferBuilder2.setSorter(VertexSorter.byDistance(cameraX - (float)blockPos.getX(), cameraY - (float)blockPos.getY(), cameraZ - (float)blockPos.getZ()));
//            renderData.translucencySortingData = bufferBuilder2.getSortingData();
//        }

        // build vbo
        val builtBuffers = mutableMapOf<RenderLayer, BuiltBuffer>()
        for (layer in nonNullLayers) {
            val builtBuffer = bufferStorage.get(layer).endNullable() ?: continue
            builtBuffers[layer] = builtBuffer
        }
    }

    fun render(buffer: VertexConsumer, matrices: MatrixStack, camera: Camera) {
        val mc = MinecraftClient.getInstance()
        val renderManager = mc.blockRenderManager
        blocks.forEach { (pos, state) ->
            matrices.push()
            matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)
            matrices.translate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            if (FANCY_BLOCK_OUTLINE.booleanValue) {
                renderManager.modelRenderer.render(
                    matrices.peek(),
                    buffer,
                    state,
                    renderManager.getModel(state),
                    1.0f,
                    1.0f,
                    1.0f,
                    15,
                    OverlayTexture.DEFAULT_UV
                )
            } else {
                val matrix4f = matrices.peek().positionMatrix

                buffer.vertex(matrix4f, 0f, 0f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 1f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 1f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 0f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()

                buffer.vertex(matrix4f, 0f, 0f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 1f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 1f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 0f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()

                buffer.vertex(matrix4f, 0f, 0f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 0f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 1f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 1f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()

                buffer.vertex(matrix4f, 1f, 0f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 0f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 1f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 1f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()

                buffer.vertex(matrix4f, 0f, 0f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 0f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 0f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 0f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()

                buffer.vertex(matrix4f, 0f, 1f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 0f, 1f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 1f, 1f).color(255, 255, 255, 255).texture(0f, 0f).next()
                buffer.vertex(matrix4f, 1f, 1f, 0f).color(255, 255, 255, 255).texture(0f, 0f).next()
            }
            matrices.pop()
        }
    }

    val blocks = mutableMapOf<BlockPos, BlockState>()
}
