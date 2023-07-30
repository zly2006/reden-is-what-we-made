package com.github.zly2006.reden.intro

import com.github.zly2006.reden.ResourceLoader
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class SuperRightIntro : Screen(Text.of("Super Right Intro")) {
    companion object {
        private val TEXTURE = ResourceLoader.loadTexture("superright/chat.png")
        const val HEIGHT = 206
        const val WIDTH = 486
    }
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        super.render(context, mouseX, mouseY, delta)
        context.drawTexture(TEXTURE, 0, 0, WIDTH / 3, HEIGHT / 3, 0F, 0F, WIDTH, HEIGHT, WIDTH, HEIGHT)
    }
}
