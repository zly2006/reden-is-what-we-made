package com.github.zly2006.reden.gui.componments

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import io.wispforest.owo.ui.base.BaseComponent
import io.wispforest.owo.ui.core.AnimatableProperty
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.PositionedRectangle
import io.wispforest.owo.ui.core.Sizing
import org.joml.Matrix4f
import kotlin.math.min

open class WebTextureComponent(
    private val texture: WebTexture,
    private val u: Int,
    private val v: Int,
    private val regionWidth: Int,
    private val regionHeight: Int,
) : BaseComponent() {
    private val visibleArea =
        AnimatableProperty.of(PositionedRectangle.of(0, 0, texture.image.width, texture.image.height))!!

    companion object {
        fun fixedHeight(texture: WebTexture, u: Int, v: Int, height: Int) = WebTextureComponent(
            texture, u, v, height * texture.image.width / texture.image.height, height
        )
    }

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
        //todo
        //? if < 1.21.5 {
        RenderSystem.enableDepthTest()

        if (this.blend) {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
        }
        //?}

        val matrices = context.pose()
        matrices.pushPose()
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
            (u + 0.0F) / texture.image.width,
            (u + visibleArea.width()) / texture.image.width,
            (v + 0.0F) / texture.image.height,
            (u + visibleArea.height()) / texture.image.height
        )

        //? if < 1.21.5 {
        if (this.blend) {
            RenderSystem.disableBlend()
        }
        //?}

        matrices.popPose()
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
        //? if <= 1.21.1 {
        RenderSystem.shaderTextures[0] = texture.id
        RenderSystem.setShader { net.minecraft.client.renderer.GameRenderer.getPositionTexShader() }
        //?} elif <= 1.21.4 {
        /*RenderSystem.setShader(net.minecraft.client.renderer.CoreShaders.POSITION_TEX)
        RenderSystem.setShaderTexture(0, texture.id)
        *///?} else {
        /*RenderSystem.setShaderTexture(0, texture.texture)
        *///?}
        val matrix4f: Matrix4f = context.pose().last().pose()
        val bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        bufferBuilder.addVertex(matrix4f, x1.toFloat(), y1.toFloat(), z.toFloat()).setUv(u1, v1)
        bufferBuilder.addVertex(matrix4f, x1.toFloat(), y2.toFloat(), z.toFloat()).setUv(u1, v2)
        bufferBuilder.addVertex(matrix4f, x2.toFloat(), y2.toFloat(), z.toFloat()).setUv(u2, v2)
        bufferBuilder.addVertex(matrix4f, x2.toFloat(), y1.toFloat(), z.toFloat()).setUv(u2, v1)
        //? if < 1.21.5
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(bufferBuilder.buildOrThrow())
        //? if >= 1.21.5
        /*1*/
    }

    fun resetVisibleArea(): WebTextureComponent {
        this.visibleArea.set(PositionedRectangle.of(0, 0, this.regionWidth, this.regionHeight))
        return this
    }
}
