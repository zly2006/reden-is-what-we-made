package com.github.zly2006.reden.render

import com.github.zly2006.reden.malilib.FANCY_BLOCK_OUTLINE
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos

@Environment(EnvType.CLIENT)
object BlockOutline {
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
                    null,
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
