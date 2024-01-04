package com.github.zly2006.reden.render

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Camera
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos

@Environment(EnvType.CLIENT)
object StructureOutline {
    fun render(buffer: VertexConsumer, matrices: MatrixStack, camera: Camera) {
        val mc = MinecraftClient.getInstance()
        val renderManager = mc.blockRenderManager
        set.forEach { (pos, state) ->
            matrices.push()
            matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)
            matrices.translate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
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
            matrices.pop()
        }
    }

    val set = mutableMapOf<BlockPos, BlockState>()
}
