package com.github.zly2006.reden.render

import com.github.zly2006.reden.malilib.BLOCK_BORDER_ALPHA
import com.github.zly2006.reden.malilib.MAX_RENDER_DISTANCE
import com.github.zly2006.reden.network.TagBlockPos
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

@Environment(EnvType.CLIENT)
object BlockBorder {
    internal var tags = mapOf<Long, Int>()

    @JvmStatic operator fun set(pos: BlockPos, status: Int?) {
        tags = if (status == null) {
            tags - pos.asLong()
        } else {
            tags + (pos.asLong() to status)
        }
    }

    @JvmStatic operator fun get(pos: BlockPos): Int {
        return tags[pos.asLong()] ?: 0
    }

    init {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register { context ->
            tags.filter { context.camera().pos.distanceTo(BlockPos.fromLong(it.key).toCenterPos()) < MAX_RENDER_DISTANCE.integerValue }
                .forEach { (posLong, status) ->
                    if (status == 0) {
                        return@register
                    }
                    val pos = BlockPos.fromLong(posLong)
                    val alpha = BLOCK_BORDER_ALPHA.doubleValue.toFloat()
                    val matrix4f = context.matrixStack()!!.peek().positionMatrix
                    RenderSystem.disableCull()
                    RenderSystem.disableScissor()
                    RenderSystem.enableBlend()
                    fun drawBox(_min: Vec3d, _max: Vec3d, r: Float, g: Float, b: Float, a: Float) {
                        val delta = 0.0001f
                        val min = _min.toVector3f().add(-delta, -delta, -delta)
                        val max = _max.toVector3f().add(delta, delta, delta)

                        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
                        val buffer =
                            Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

                        buffer.vertex(matrix4f, min.x, min.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, min.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, max.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, max.y, min.z).color(r, g, b, a)

                        buffer.vertex(matrix4f, max.x, min.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, min.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, max.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, max.y, min.z).color(r, g, b, a)

                        buffer.vertex(matrix4f, min.x, min.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, min.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, min.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, min.y, max.z).color(r, g, b, a)

                        buffer.vertex(matrix4f, min.x, max.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, max.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, max.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, max.y, max.z).color(r, g, b, a)

                        buffer.vertex(matrix4f, min.x, min.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, max.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, max.y, min.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, min.y, min.z).color(r, g, b, a)

                        buffer.vertex(matrix4f, min.x, min.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, min.x, max.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, max.y, max.z).color(r, g, b, a)
                        buffer.vertex(matrix4f, max.x, min.y, max.z).color(r, g, b, a)

                        BufferRenderer.drawWithGlobalProgram(buffer.end())
                    }
                    when (status) {
                        TagBlockPos.green -> drawBox(
                            pos.vec3d() - context.camera().pos,
                            pos.add(1, 1, 1).vec3d() - context.camera().pos,
                            0f,
                            1f,
                            0f,
                            alpha
                        )

                        TagBlockPos.red -> drawBox(
                            pos.vec3d() - context.camera().pos,
                            pos.add(1, 1, 1).vec3d() - context.camera().pos,
                            1f,
                            0f,
                            0f,
                            alpha
                        )

                        else -> drawBox(
                            pos.vec3d() - context.camera().pos,
                            pos.add(1, 1, 1).vec3d() - context.camera().pos,
                            0f,
                            0f,
                            0f,
                            alpha
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
