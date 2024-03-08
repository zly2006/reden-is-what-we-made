package com.github.zly2006.reden.gui

import com.github.zly2006.reden.report.updateOnlineInfo
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class LoginRedenScreen(
    val parent: Screen? = MinecraftClient.getInstance().currentScreen,
    val reason: Text? = null
) : BaseOwoScreen<FlowLayout>() {
    private val status = Components.label(Text.empty())!!
    private val buttonClose = Components.button(Text.literal("Close")) {
        close()
    }!!
    private val usernameText = Components.textBox(Sizing.fill())!!.apply {
        setPlaceholder(Text.of("Reden Username"))
    }
    private val passwordText = Components.textBox(Sizing.fill())!!.apply {
        setPlaceholder(Text.of("Password"))
    }
    private val loginButton = Components.button(Text.literal("Login")) {
        TODO()
    }!!
    override fun createAdapter() = OwoUIAdapter.create(this, Containers::verticalFlow)!!

    override fun build(rootComponent: FlowLayout) {
        rootComponent
            .gap(5)
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
        rootComponent
            .child(Components.label(Text.literal("Login to Reden")).apply {
                maxWidth(400)
                margins(Insets.bottom(10))
            })
            .child(Components.label(reason ?: Text.literal("Your operation requires login to Reden")).maxWidth(400))
            .child(
                Components.label(Text.literal("Please first bind your Microsoft account to Reden and verify your Minecraft ownership, then we can log you in automatically"))
                    .maxWidth(400).horizontalTextAlignment(HorizontalAlignment.CENTER)
            )
            .child(status.apply {
                maxWidth(400)
                margins(Insets.bottom(10))
            })
            .child(Components.button(Text.literal("Auto Login")) {
                it.active(false)
                if (updateOnlineInfo(client!!)) {
                    status.text(Text.literal("Login success").formatted(Formatting.GREEN))
                    (status.parent() as FlowLayout).child(buttonClose)
                }
                else {
                    status.text(Text.literal("Login failed").formatted(Formatting.RED))
                    it.active(true)
                }
            })
            .child(Components.label(Text.literal("Or login manually")).maxWidth(400))
            .child(usernameText)
            .child(passwordText)
            .child(loginButton)
    }

    override fun close() {
        client!!.setScreen(parent)
    }
}
