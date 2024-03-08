package com.github.zly2006.reden.gui

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.report.*
import com.github.zly2006.reden.utils.red
import com.github.zly2006.reden.utils.redenApiBaseUrl
import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import okhttp3.Request

class LoginRedenScreen(
    val parent: Screen? = MinecraftClient.getInstance().currentScreen,
    val reason: Text? = null
) : BaseOwoScreen<FlowLayout>() {
    private val status = Components.label(Text.empty())!!
    private val buttonClose = Components.button(Text.literal("Close")) {
        close()
    }!!
    private val usernameText = Components.textBox(Sizing.fixed(400))!!.apply {
        setPlaceholder(Text.of("Reden Username"))
    }
    private val passwordText = Components.textBox(Sizing.fixed(400))!!.apply {
        setPlaceholder(Text.of("Password"))
    }
    private val loginButton = Components.button(Text.literal("Login")) {
        @Serializable
        class Req(
            val key: String,
            val username: String,
            val password: String
        )

        @Serializable
        class Response(
            val success: Boolean,
            val username: String,
            val error: String,
            val email: String,
            val isStaff: Boolean
        )
        httpClient.newCall(Request.Builder().apply {
            if (usernameText.text.isEmpty() || passwordText.text.isEmpty()) {
                status.text(Text.literal("Username or password is null").red())
            }
            url("$redenApiBaseUrl/mc/login")
            ua()
            json(Req(key, usernameText.text, passwordText.text))
        }.build()).execute().use {
            if (it.isSuccessful) {
                status.text(Text.literal("Login success").formatted(Formatting.GREEN))
                (status.parent() as FlowLayout).child(buttonClose)
            }
            else {
                val responseText = it.body!!.string()
                try {
                    val res = Json.decodeFromString<Response>(responseText)
                    status.text(Text.literal(res.error))
                } catch (e: Exception) {
                    Reden.LOGGER.error("Failed to load response: $responseText", e)
                    status.text(Text.literal("${it.code} ${it.message}"))
                }
            }
        }
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
