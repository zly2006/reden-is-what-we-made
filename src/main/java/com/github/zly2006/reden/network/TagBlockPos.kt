package com.github.zly2006.reden.network

import com.github.zly2006.reden.isClient
import com.github.zly2006.reden.malilib.MAX_RENDER_DISTANCE
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.networking.v1.FabricPacket
import net.fabricmc.fabric.api.networking.v1.PacketType
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

private val pType = run {
    PacketType.create(TAG_BLOCK_POS) {
        TagBlockPos(
            it.readIdentifier(),
            it.readBlockPos(),
            it.readVarInt()
        )
    }

}

class TagBlockPos(
    val world: Identifier,
    val pos: BlockPos,
    val status: Int
): FabricPacket {
    override fun getType(): PacketType<*> = pType
    override fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(world)
        buf.writeBlockPos(pos)
        buf.writeVarInt(status)
    }

    companion object {
        const val clear = 0
        const val green = 1
        const val red = 2

        internal val tags = mutableMapOf<Long, Int>()

        fun register() {
            if (!isClient) return
            ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> tags.clear()}
            ClientPlayNetworking.registerGlobalReceiver(pType) { packet, _, _ ->
                tags[packet.pos.asLong()] = packet.status
            }
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
                            green -> drawBox(
                                pos.vec3d() - context.camera().pos,
                                pos.add(1, 1, 1).vec3d() - context.camera().pos,
                                0f,
                                1f,
                                0f,
                                0.5f
                            )

                            red -> drawBox(
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
}

private operator fun Vec3d.minus(pos: Vec3d): Vec3d {
    return Vec3d(x - pos.x, y - pos.y, z - pos.z)
}

private fun BlockPos.vec3d() = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
