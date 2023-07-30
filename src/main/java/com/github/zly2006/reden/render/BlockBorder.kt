package com.github.zly2006.reden.render

import com.github.zly2006.reden.malilib.MAX_RENDER_DISTANCE
import com.github.zly2006.reden.network.TagBlockPos
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

@Environment(EnvType.CLIENT)
object BlockBorder {
    internal val tags = mutableMapOf<Long, Int>()

    init {
        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            tags.filter { context.camera().pos.distanceTo(BlockPos.fromLong(it.key).toCenterPos()) < MAX_RENDER_DISTANCE.integerValue }
                .forEach { (_pos, status) ->
                    if (status == 0) {
                        tags.remove(_pos)
                        return@register
                    }
                    val pos = BlockPos.fromLong(_pos)
                    val matrix4f = context.matrixStack().peek().positionMatrix
                    RenderSystem.disableCull()
                    RenderSystem.disableScissor()
                    RenderSystem.enableBlend()
                    fun drawBox(_min: Vec3d, _max: Vec3d, r: Float, g: Float, b: Float, a: Float) {
                        val delta = 0.0001f
                        val min = _min.toVector3f().add(-delta, -delta, -delta)
                        val max = _max.toVector3f().add(delta, delta, delta)

                        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
                        Tessellator.getInstance().buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, min.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, min.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, max.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, max.y, min.z).color(r, g, b, a).next()

                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, min.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, min.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, max.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, max.y, min.z).color(r, g, b, a).next()

                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, min.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, min.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, min.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, min.y, max.z).color(r, g, b, a).next()

                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, max.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, max.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, max.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, max.y, max.z).color(r, g, b, a).next()

                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, min.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, max.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, max.y, min.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, min.y, min.z).color(r, g, b, a).next()

                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, min.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, min.x, max.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, max.y, max.z).color(r, g, b, a).next()
                        Tessellator.getInstance().buffer.vertex(matrix4f, max.x, min.y, max.z).color(r, g, b, a).next()

                        Tessellator.getInstance().draw()
                    }
                    when (status) {
                        TagBlockPos.green -> drawBox(
                            pos.vec3d() - context.camera().pos,
                            pos.add(1, 1, 1).vec3d() - context.camera().pos,
                            0f,
                            1f,
                            0f,
                            0.5f
                        )

                        TagBlockPos.red -> drawBox(
                            pos.vec3d() - context.camera().pos,
                            pos.add(1, 1, 1).vec3d() - context.camera().pos,
                            1f,
                            0f,
                            0f,
                            0.5f
                        )

                        else -> drawBox(
                            pos.vec3d() - context.camera().pos,
                            pos.add(1, 1, 1).vec3d() - context.camera().pos,
                            0f,
                            0f,
                            0f,
                            0.5f
                        )
                    }
                    RenderSystem.enableCull()
                    RenderSystem.disableBlend()
                }
        }
    }
}

private operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}

private fun BlockPos.vec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
