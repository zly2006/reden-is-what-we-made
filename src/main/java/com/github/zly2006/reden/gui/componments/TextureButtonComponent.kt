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
        val i = this.x + this.getWidth() / 2 - this.textureWidth / 2
        val j = this.y + this.getHeight() / 2 - this.textureHeight / 2
        context.drawTexture(texture, i, j, 0f, 0f, textureWidth, textureHeight, textureWidth, textureHeight)
        if (hovered) {
            val mc = MinecraftClient.getInstance()
            context.drawTooltip(mc.textRenderer, tooltip, mouseX, mouseY)
        }
    }
}
