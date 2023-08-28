package com.github.zly2006.reden.rvc.remote.github

import com.github.zly2006.reden.malilib.GITHUB_TOKEN
import com.github.zly2006.reden.utils.buttonWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class GithubAuthScreen: Screen(Text.of("Login to Github")) {
    val auth = GithubAuth()
    val login2GithubButton = buttonWidget(10, 10, 100, 20, Text.of("Login to Github")) {
        auth.genCode()
        auth.startPoll {
            GITHUB_TOKEN.setValueFromString("Bearer ${it.token}")
        }
    }

    override fun init() {
        addDrawableChild(login2GithubButton)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        login2GithubButton.active = auth.canRelogin
        super.render(context, mouseX, mouseY, delta)
        context.drawText(client!!.textRenderer, auth.authState.name, 10, 50, 0xffffff, true)
    }
}