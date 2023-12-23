package com.github.zly2006.reden.gui.componments

import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.TextIconButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class TextureButtonComponent(
    texture: Identifier,
    onPress: PressAction,
    width: Int = 20,
    height: Int = width,
    textureWidth: Int = 16,
    textureHeight: Int = textureWidth,
    var tooltip: Text? = null
): TextIconButtonWidget(width, height, Text.empty(), textureWidth, textureHeight, texture, onPress) {
    override fun drawMessage(context: DrawContext?, textRenderer: TextRenderer?, color: Int) {
        super.drawMessage(context, textRenderer, color)
    }

    public override fun renderWidget(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(context, mouseX, mouseY, delta)
        val i = this.x + this.getWidth() / 2.0
        val j = this.y + this.getHeight() / 2.0
        context.push()
        context.translate(i, j, 0.0)
        context.scale((width - 2.0f) / textureWidth, (height - 2.0f) / textureHeight, 0.0F)
        context.drawTexture(texture,-this.textureWidth / 2, -this.textureHeight / 2, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight)
        context.pop()
        if (hovered && tooltip != null) {
            val mc = MinecraftClient.getInstance()
            context.drawTooltip(mc.textRenderer, tooltip, mouseX, mouseY)
        }
    }
}
