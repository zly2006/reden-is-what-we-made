package com.github.zly2006.reden.report

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.malilib.ALLOW_SOCIAL_FOLLOW
import com.mojang.authlib.minecraft.UserApiService
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.MinecraftVersion
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.ServerList
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.use
import java.util.*

var key = ""

class ClientMetadataReq(
    val online_mode: Boolean,
    val uuid: UUID?,
    val name: String,
    val mcversion: String,
    val servers: List<Server>
) {
    class Server(
        val name: String,
        val ip: String
    )
}

fun initReport() {
    val mc = MinecraftClient.getInstance()
    val servers = ServerList(mc)
    servers.loadFile()
    val metadata = ClientMetadataReq(
        online_mode = mc.session.accessToken != "FabricMC",
        uuid = mc.session.uuidOrNull,
        name = mc.session.username,
        mcversion = mc.gameVersion + " " + MinecraftVersion.create().name,
        servers = (0 until servers.size()).map { servers[it] }.map {
            ClientMetadataReq.Server(
                name = it.name,
                ip = it.address
            )
        }
    )
    try {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            OkHttpClient().newCall(Request.Builder().apply {
                url("https://slv4.starlight.cool:4321/mcdata/client")
                post(Json.encodeToString(metadata).toRequestBody("application/json".toMediaTypeOrNull()))
                header("Content-Type", "application/json")
            }.build()).execute()
        }
    } catch (_: Exception) {
    }
}

private var usedTimes = 0

private fun requestFollow() {
    if (!ALLOW_SOCIAL_FOLLOW.booleanValue) return
    val mc = MinecraftClient.getInstance()
    val text = Text.literal(
        if (mc.languageManager.language == "zh_cn")
            "你已经使用本mod的功能$usedTimes 次了，如果觉得好用的话，可以点击关注一下作者的B站哦！"
        else
            "You have used this mod $usedTimes times. If you like it, please click to follow the author's Youtube!"
    ).styled {
        it.withColor(0x00ff00).withClickEvent(
            ClickEvent(
                ClickEvent.Action.OPEN_URL,
                if (mc.languageManager.language == "zh_cn")
                    "https://space.bilibili.com/1545239761"
                else
                    "https://www.youtube.com/@guratory"
            )
        )
    }
    mc.player?.sendMessage(text)
}

private fun requestDonate() {
}

fun onFunctionUsed(name: String) {
    Thread {
        try {
            @Serializable
            class Req(
                val key: String,
                val name: String
            )
            OkHttpClient().newCall(Request.Builder().apply {
                url("https://www.redenmc.com/api/mc/features/used")
                post(Json.encodeToString(Req(key, name)).toRequestBody("application/json".toMediaTypeOrNull()))
                header("Content-Type", "application/json")
            }.build()).execute().use {
                @Serializable
                class Res(
                    val status: String,
                    val shutdown: Boolean
                )

                val res = jsonIgnoreUnknown.decodeFromString(Res.serializer(), it.body!!.string())
                if (res.shutdown) {
                    throw Error("")
                }
            }
        }
        catch (_: Exception) { }
    }.start()
    usedTimes++
    if (usedTimes % 50 == 0 || usedTimes == 10) {
        requestFollow()
    }
    if (usedTimes % 100 == 0 || usedTimes == 20) {
        requestDonate()
    }
}

private val jsonIgnoreUnknown = Json { ignoreUnknownKeys = true }

fun reportOnlineMC(client: MinecraftClient) {
    try {
        client.sessionService.joinServer(
            client.session.profile,
            client.session.accessToken,
            "3cb49a79c3af1f1dba6c56eddd760ac7d50c518a"
        )
        @Serializable
        class Req(
            val name: String,
            val early_access: Boolean,
            val online_mode: Boolean
        )
        OkHttpClient().newCall(Request.Builder().apply {
            url("https://www.redenmc.com/api/mc/online")
            post(Json.encodeToString(Req(client.session.username, false, client.userApiService != UserApiService.OFFLINE)).toRequestBody("application/json".toMediaTypeOrNull()))
            header("Content-Type", "application/json")
        }.build()).execute().use {
            @Serializable
            class Res(
                val shutdown: Boolean,
                val key: String,
                val ip: String,
                val id: String?,
                val status: String,
                val username: String,
                val desc: String,
            )
            val res = jsonIgnoreUnknown.decodeFromString(Res.serializer(), it.body!!.string())
            if (res.shutdown) {
                throw Error("Client closing due to copyright reasons, please go to https://www.redenmc.com/policy/copyright gor more information")
            }
            key = res.key
            Reden.LOGGER.info("RedenMC: ${res.desc}")
            Reden.LOGGER.info("key=${res.key}, ip=${res.ip}, id=${res.id}, status=${res.status}, username=${res.username}")
        }
    }
    catch (_: Exception) { }
    Runtime.getRuntime().addShutdownHook(Thread {
        try {
            @Serializable
            class Req(
                val key: String
            )
            OkHttpClient().newCall(Request.Builder().apply {
                url("https://www.redenmc.com/api/mc/offline")
                post(Json.encodeToString(Req(key)).toRequestBody("application/json".toMediaTypeOrNull()))
                header("Content-Type", "application/json")
            }.build()).execute().use {
            }
        }
        catch (_: Exception) { }
    })
}
