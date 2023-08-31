package com.github.zly2006.reden.report

import com.github.zly2006.reden.malilib.ALLOW_SOCIAL_FOLLOW
import com.google.gson.Gson
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.MinecraftVersion
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.MultiplayerServerListPinger
import net.minecraft.client.option.ServerList
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.http.HttpClient
import java.util.*
import java.util.concurrent.CompletableFuture

class ClientMetadataReq(
    val online_mode: Boolean,
    val uuid: UUID?,
    val name: String,
    val mcversion: String,
    val servers: List<Server>
) {
    class Server(
        val name: String,
        val ip: String,
        val motd: String
    )
}

fun initReport() {
    val httpClient = HttpClient.newHttpClient()
    val mc = MinecraftClient.getInstance()
    val servers = ServerList(mc)
    servers.loadFile()
    val serverPinger = MultiplayerServerListPinger()
    val metadata = ClientMetadataReq(
        online_mode = mc.session.accessToken != "FabricMC",
        uuid = mc.session.uuidOrNull,
        name = mc.session.username,
        mcversion = mc.gameVersion + " " + MinecraftVersion.create().name,
        servers = (0 until servers.size()).map { servers[it] }.map {
            val future = CompletableFuture<ClientMetadataReq.Server>()
            serverPinger.add(it) {
                future.complete(
                    ClientMetadataReq.Server(
                        name = it.name,
                        ip = it.address,
                        motd = "${it.label}, online=${it.online}, players=${it.playerCountLabel}"
                    )
                )
            }
            future
        }.map { it.join() }
    )
    try {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            OkHttpClient().newCall(Request.Builder().apply {
                url("https://slv4.starlight.cool:4321/mcdata/client")
                post(Gson().toJson(metadata).toRequestBody("application/json".toMediaTypeOrNull()))
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
        // usage report
    }.start()
    usedTimes++
    if (usedTimes % 50 == 0 || usedTimes == 10) {
        requestFollow()
    }
    if (usedTimes % 100 == 0 || usedTimes == 20) {
        requestDonate()
    }
}
