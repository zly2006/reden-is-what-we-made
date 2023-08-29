package com.github.zly2006.reden.rvc.remote.github

import com.github.zly2006.reden.malilib.GITHUB_TOKEN
import com.github.zly2006.reden.utils.buttonWidget
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import okhttp3.OkHttpClient
import okhttp3.Request

private val ignoringJson = Json { ignoreUnknownKeys = true }

class GithubAuthScreen: Screen(Text.of("Login to Github")) {
    val auth = GithubAuth()
    var userName: String? = null
    var userId: String? = null
    init {
        if (GITHUB_TOKEN.stringValue.isNotEmpty()) {
            Thread {
                try {
                    refreshUser()
                } catch (_: Exception) {
                }
            }.start()
        }
    }

    private fun refreshUser() {
        val response = OkHttpClient().newCall(
            Request.Builder().url("https://api.github.com/user")
                .header("Authorization", GITHUB_TOKEN.stringValue)
                .header("Accept", "application/json")
                .build()
        ).execute()
        if (response.code == 200) {
            auth.authState = GithubAuth.AuthState.FINISHED
        } else {
            auth.authState = GithubAuth.AuthState.FAILED
        }
        @Serializable
        class Response(val login: String, val name: String)
        ignoringJson.decodeFromString(Response.serializer(), response.body!!.string()).let {
            userName = it.name
            userId = it.login
        }
    }

    val login2GithubButton = buttonWidget(10, 10, 100, 20, Text.of("Login to Github")) {
        auth.genCode()
        auth.startPoll {
            GITHUB_TOKEN.setValueFromString("Bearer ${it.token}")
            Thread {
                try {
                    refreshUser()
                } catch (_: Exception) {
                }
            }.start()
        }
    }
    val cancelLogin = buttonWidget(10, 40, 100, 20, Text.of("Cancel Login")) {
        if (auth.authState == GithubAuth.AuthState.POLLING) {
            auth.authState = GithubAuth.AuthState.FAILED
        }
    }

    override fun init() {
        addDrawableChild(login2GithubButton)
        addDrawableChild(cancelLogin)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        cancelLogin.visible = auth.authState == GithubAuth.AuthState.POLLING
        login2GithubButton.active = auth.canRelogin
        super.render(context, mouseX, mouseY, delta)
        context.drawText(client!!.textRenderer, auth.authState.name, 10, 50, 0xffffff, true)
        if (auth.authState == GithubAuth.AuthState.POLLING) {
            context.drawText(client!!.textRenderer, "Expires in ${(auth.expiresAt - System.currentTimeMillis()) / 1000} seconds", 10, 60, 0xffffff, true)
            context.drawText(client!!.textRenderer, "User code: ${auth.getCodeResponse!!.user_code}, this is copied to your clipboard, paste it to the browser", 10, 70, 0xffffff, true)
        }
        if (auth.authState == GithubAuth.AuthState.FINISHED) {
            context.drawText(client!!.textRenderer, "Logged in as ${userName}(${userId})", 10, 60, 0xffffff, true)
        }
    }

    override fun close() {
        auth.authState = GithubAuth.AuthState.FAILED
        super.close()
    }
}