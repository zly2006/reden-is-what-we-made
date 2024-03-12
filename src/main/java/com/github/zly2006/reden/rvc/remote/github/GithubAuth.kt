package com.github.zly2006.reden.rvc.remote.github

import com.github.zly2006.reden.malilib.GITHUB_TOKEN
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Util
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

val CLIENT_ID: String = TODO()
val CONNECTION = "https://github.com/settings/connections/applications/:$CLIENT_ID"
class GithubAuth {
    val canRelogin: Boolean
        get() = authState == AuthState.EXPIRED || authState == AuthState.FAILED || authState == AuthState.NONE || authState == AuthState.FINISHED
    val client = OkHttpClient()
    @Serializable
    @Suppress("PropertyName")
    class GetCodeResponse(
        val device_code: String,
        val user_code: String,
        val verification_uri: String,
        val expires_in: Int,
        val interval: Int,
    )
    enum class AuthState {
        NONE,
        FAILED,
        EXPIRED,
        FINISHED,
        GOT_CODE,
        POLLING,
    }
    var getCodeResponse: GetCodeResponse? = null
    var authState = if (GITHUB_TOKEN.stringValue.isEmpty()) AuthState.NONE else AuthState.FINISHED
    var expiresAt = 0L
    fun genCode() {
        val response = client.newCall(
            Request.Builder().url("https://github.com/login/device/code").post(
                Json.encodeToString(
                    mapOf(
                        "client_id" to CLIENT_ID,
                        "scope" to "repo, "
                    )
                ).toRequestBody("application/json".toMediaType())
            ).header("Accept", "application/json").build()
        ).execute()
        getCodeResponse = Json.decodeFromString(GetCodeResponse.serializer(), response.body!!.string())
        val mc = MinecraftClient.getInstance()
        authState = AuthState.GOT_CODE
        expiresAt = System.currentTimeMillis() + getCodeResponse!!.expires_in * 1000L
        mc.keyboard.clipboard = getCodeResponse!!.user_code
        Util.getOperatingSystem().open(getCodeResponse!!.verification_uri)
    }

    private fun poll(): LoginResult? {
        if (authState != AuthState.POLLING) {
            throw IllegalStateException("Cannot poll without starting polling")
        }
        @Serializable
        @Suppress("PropertyName")
        class Response (
            val access_token: String,
            val token_type: String,
            val scope: String
        )
        try {
            val response = client.newCall(
                Request.Builder().url("https://github.com/login/oauth/access_token").post(
                    Json.encodeToString(
                        mapOf(
                            "client_id" to CLIENT_ID,
                            "device_code" to getCodeResponse!!.device_code,
                            "grant_type" to "urn:ietf:params:oauth:grant-type:device_code"
                        )
                    ).toRequestBody("application/json".toMediaType())
                ).header("Accept", "application/json").build()
            ).execute()
            val result = Json.decodeFromString(Response.serializer(), response.body!!.string())
            authState = AuthState.FINISHED
            return LoginResult().apply {
                token = result.access_token
            }
        }
        catch (e: Exception) {
            return null
        }
    }

    class LoginResult {
        var token: String? = null
        var error: String? = null
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startPoll(consumer: (LoginResult) -> Unit) {
        if (authState != AuthState.GOT_CODE) {
            throw IllegalStateException("Cannot start polling without a code")
        }
        authState = AuthState.POLLING
        val authJob = GlobalScope.async(Dispatchers.IO) {
            while (authState == AuthState.POLLING) {
                val result = poll()
                delay(getCodeResponse!!.interval * 1000L)
                if (result != null) {
                    consumer(result)
                }
                if (System.currentTimeMillis() > expiresAt) {
                    authState = AuthState.EXPIRED
                }
            }
            return@async authState
        }
    }
}
