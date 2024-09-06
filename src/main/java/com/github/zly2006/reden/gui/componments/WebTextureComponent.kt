package com.github.zly2006.reden.gui.componments

import com.mojang.blaze3d.systems.RenderSystem
import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.AnimatableProperty
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.PositionedRectangle
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix4f
import kotlin.math.min

open class WebTextureComponent : BaseComponent {
    private val u: Int
    private val v: Int
    private val regionWidth: Int
    private val regionHeight: Int

    constructor(
        bytes: ByteArray, u: Int, v: Int, regionWidth: Int, regionHeight: Int = WebTexture(bytes).image!!.run {
            regionWidth * height / width
        }
    ) : super() {
        this.u = u
        this.v = v
        this.regionWidth = regionWidth
        this.regionHeight = regionHeight
        this.texture = WebTexture(bytes)
        this.visibleArea =
            AnimatableProperty.of(PositionedRectangle.of(0, 0, texture.image!!.width, texture.image!!.height))!!
    }

    constructor(
        bytes: ByteArray, u: Int, v: Int, regionWidth: Int
    ) : super() {
        this.u = u
        this.v = v
        this.regionWidth = regionWidth
        this.texture = WebTexture(bytes)
        this.regionHeight = texture.image!!.run {
            regionWidth * height / width
        }
        this.visibleArea =
            AnimatableProperty.of(PositionedRectangle.of(0, 0, texture.image!!.width, texture.image!!.height))!!
    }

    private val texture: WebTexture
    private val visibleArea: AnimatableProperty<PositionedRectangle>
    var blend: Boolean = false

    override fun determineHorizontalContentSize(sizing: Sizing): Int {
        return this.regionWidth
    }

    override fun determineVerticalContentSize(sizing: Sizing): Int {
        return this.regionHeight
    }

    override fun update(delta: Float, mouseX: Int, mouseY: Int) {
        super.update(delta, mouseX, mouseY)
        visibleArea.update(delta)
    }

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        RenderSystem.enableDepthTest()

        if (this.blend) {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
        }

        val matrices = context.matrices
        matrices.push()
        matrices.translate(x.toFloat(), y.toFloat(), 0f)
        matrices.scale(this.width / regionWidth.toFloat(), this.height / regionHeight.toFloat(), 0f)

        val visibleArea = visibleArea.get()

        val bottomEdge = min((visibleArea.y() + visibleArea.height()).toDouble(), regionHeight.toDouble())
            .toInt()
        val rightEdge = min((visibleArea.x() + visibleArea.width()).toDouble(), regionWidth.toDouble())
            .toInt()

        val u = (this.u + visibleArea.x()).toFloat()
        val v = (this.v + visibleArea.y()).toFloat()
        this.drawTexturedQuad(
            context,
            visibleArea.x(),
            rightEdge,
            visibleArea.y(),
            bottomEdge,
            0,
            (u + 0.0F) / texture.image!!.width,
            (u + visibleArea.width()) / texture.image!!.width,
            (v + 0.0F) / texture.image!!.height,
            (u + visibleArea.height()) / texture.image!!.height
        )

        if (this.blend) {
            RenderSystem.disableBlend()
        }

        matrices.pop()
    }

    private fun drawTexturedQuad(
        context: OwoUIDrawContext,
        x1: Int,
        x2: Int,
        y1: Int,
        y2: Int,
        z: Int,
        u1: Float,
        u2: Float,
        v1: Float,
        v2: Float
    ) {
        RenderSystem.shaderTextures[0] = texture.glId
        RenderSystem.setShader { GameRenderer.getPositionTexProgram() }
        val matrix4f: Matrix4f = context.matrices.peek().positionMatrix
        val bufferBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)
        bufferBuilder.vertex(matrix4f, x1.toFloat(), y1.toFloat(), z.toFloat()).texture(u1, v1)
        bufferBuilder.vertex(matrix4f, x1.toFloat(), y2.toFloat(), z.toFloat()).texture(u1, v2)
        bufferBuilder.vertex(matrix4f, x2.toFloat(), y2.toFloat(), z.toFloat()).texture(u2, v2)
        bufferBuilder.vertex(matrix4f, x2.toFloat(), y1.toFloat(), z.toFloat()).texture(u2, v1)
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
    }

    fun resetVisibleArea(): WebTextureComponent {
        this.visibleArea.set(PositionedRectangle.of(0, 0, this.regionWidth, this.regionHeight))
        return this
    }
}
