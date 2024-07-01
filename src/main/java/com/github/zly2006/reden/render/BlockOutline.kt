package com.github.zly2006.reden.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import org.joml.Matrix4f

@Environment(EnvType.CLIENT)
object BlockOutline {
    fun render(buffer: VertexConsumer, matrices: Matrix4f, camera: Camera, vertexConsumers: VertexConsumerProvider) {
        val mc = MinecraftClient.getInstance()
        require(mc.isOnThread) {
            "BlockOutline.render must be called on the client thread"
        }
        val renderManager = mc.blockRenderManager
        val random = Random.create()
        synchronized(blocks) {
            blocks.toList()
        }.forEach { (pos, state) ->
            if (state.fluidState != null) {
                renderManager.renderFluid(
                    pos,
                    mc.world,
                    buffer,
                    state,
                    state.fluidState
                )
            }

            val matrixStack = MatrixStack()
            matrixStack.multiplyPositionMatrix(matrices)
            matrixStack.translate((-camera.pos.x).toFloat(), (-camera.pos.y).toFloat(), (-camera.pos.z).toFloat())
            matrixStack.translate(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())
            when (state.renderType) {
                BlockRenderType.MODEL                -> renderManager.renderBlock(
                    state,
                    pos,
                    mc.world,
                    matrixStack,
                    buffer,
                    false,
                    random
                )

                BlockRenderType.ENTITYBLOCK_ANIMATED -> {
                    mc.blockEntityRenderDispatcher.render(
                        (state.block as BlockEntityProvider).createBlockEntity(pos, state),
                        mc.renderTickCounter.getTickDelta(false),
                        matrixStack
                    ) { buffer }
                }

                else                                 -> {}
            }
        }
    }

    var color: Int = 0xffffff
    var blocks = mapOf<BlockPos, BlockState>()
        get() {
            assert(MinecraftClient.getInstance().isOnThread)
            return field
        }
        set(value) {
            assert(MinecraftClient.getInstance().isOnThread)
            field = value
        }
}
