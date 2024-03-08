package com.github.zly2006.reden.report

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.Reden.LOGGER
import com.github.zly2006.reden.gui.message.ClientMessageQueue
import com.github.zly2006.reden.malilib.HiddenOption
import com.github.zly2006.reden.malilib.HiddenOption.data_BASIC
import com.github.zly2006.reden.malilib.HiddenOption.data_IDENTIFICATION
import com.github.zly2006.reden.malilib.HiddenOption.data_USAGE
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.isDevVersion
import com.github.zly2006.reden.utils.redenApiBaseUrl
import com.github.zly2006.reden.utils.server
import com.mojang.authlib.minecraft.UserApiService
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.MinecraftVersion
import net.minecraft.SharedConstants
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.ServerList
import net.minecraft.server.MinecraftServer
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.util.Util
import net.minecraft.util.crash.CrashMemoryReserve
import net.minecraft.util.crash.CrashReport
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.userAgent
import okio.use
import java.net.URI
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

var key = ""

val httpClient = OkHttpClient.Builder().apply {
    readTimeout(60.seconds.toJavaDuration())
}.build()

inline fun <reified T> Request.Builder.json(data: T) = apply {
    header("Content-Type", "application/json")
    post(Json.encodeToString(data).toRequestBody("application/json".toMediaTypeOrNull()))
}

fun Request.Builder.ua() = apply {
    header("Authorization", "ApiKey $key")
    header("User-Agent", "RedenMC/${Reden.MOD_VERSION} Minecraft/${MinecraftVersion.create().name} (Fabric) $userAgent")
}

@Serializable
class FeatureUsageData(
    val source: String,
    val name: String,
    val time: Long,
)

fun doHeartHeat() {
    if (!data_USAGE.booleanValue || !data_BASIC.booleanValue) return
    httpClient.newCall(Request.Builder().apply {
        url("$redenApiBaseUrl/mc/heartbeat")
        @Serializable
        class Player(
            val name: String,
            val uuid: String,
            val latency: Int,
            val gamemode: String,
        )
        @Serializable
        class Req(
            val key: String,
            val usage: List<FeatureUsageData>,
            val times: Int,
            val players: List<Player>?
        )
        fun samplePlayers() = if (isClient) {
            MinecraftClient.getInstance().networkHandler?.playerList?.map { Player(
                it.profile.name,
                it.profile.id.toString(),
                it.latency,
                it.gameMode.name,
            ) }
        } else {
            server.playerManager.playerList.map {
                Player(
                    it.gameProfile.name,
                    it.gameProfile.id.toString(),
                    it.networkHandler.latency,
                    it.interactionManager.gameMode.name,
                )
            }
        }
        val req = Req(
            key,
            featureUsageData,
            usedTimes,
            if (data_IDENTIFICATION.booleanValue) samplePlayers()
            else listOf()
        )
        json(req)
        ua()
    }.build()).execute().use {
        @Serializable
        class Res(
            val status: String,
            val shutdown: Boolean,
        )

        val res = jsonIgnoreUnknown.decodeFromString(Res.serializer(), it.body!!.string())
        if (res.shutdown) {
            throw Error(res.status)
        }
        if (it.code == 200) {
            featureUsageData.clear()
            if (res.status.startsWith("set-key="))
                key = res.status.substring(8)
        }
    }
}

val featureUsageData = mutableListOf<FeatureUsageData>()
var heartbeatThread: Thread? = null
fun initHeartBeat() {
    try {
        heartbeatThread?.interrupt()
    } catch (e: Exception) {
        LOGGER.error("Failed to stop heartbeat", e)
    }
    heartbeatThread = Thread("RedenMC HeartBeat") {
        while (true) {
            try {
                Thread.sleep(1000 * 60 * 5)
                doHeartHeat()
            } catch (e: InterruptedException) {
                break
            } catch (e: Exception) {
                LOGGER.error("", e)
            }
        }
    }
    heartbeatThread!!.start()
}

fun Thread(name: String, function: () -> Unit) = Thread(function, name)

private var usedTimes = 0

private fun requestFollow() {
    val mc = MinecraftClient.getInstance()
    val key = "reden:youtube"
    val buttonList = mutableListOf<ClientMessageQueue.Button>()
    val id = ClientMessageQueue.addNotification(
        key,
        Reden.LOGO,
        Text.translatable("reden.message.youtube.title"),
        Text.translatable("reden.message.youtube.desc", usedTimes),
        buttonList
    )
    buttonList.add(
        ClientMessageQueue.Button(Text.translatable("reden.message.youtube.yes")) {
            Util.getOperatingSystem().open(
                URI(
                    if (mc.languageManager.language == "zh_cn")
                        "https://space.bilibili.com/1545239761"
                    else
                        "https://www.youtube.com/@zly2006"
                )
            )
            ClientMessageQueue.dontShowAgain(key)
            ClientMessageQueue.remove(id)
        }
    )
    buttonList.add(
        ClientMessageQueue.Button(Text.translatable("reden.message.youtube.no")) {
            ClientMessageQueue.dontShowAgain(key)
            ClientMessageQueue.remove(id)
        }
    )
}

private fun requestDonate() {
}

fun onFunctionUsed(name: String) {
    featureUsageData.add(FeatureUsageData(if (isClient) MinecraftClient.getInstance().session.username else "Server", name, System.currentTimeMillis()))
    if (heartbeatThread == null || !heartbeatThread!!.isAlive) {
        initHeartBeat()
    }
    usedTimes++
    if (isClient) {
        if (usedTimes % 50 == 0 || usedTimes == 10) {
            requestFollow()
        }
        if (usedTimes % 100 == 0 || usedTimes == 20) {
            requestDonate()
        }
    }
}

private val jsonIgnoreUnknown = Json { ignoreUnknownKeys = true }

fun reportServerStart(server: MinecraftServer) {

}

fun reportException(e: Exception) {
    if (isDevVersion && data_USAGE.booleanValue) {
        try {
            CrashMemoryReserve.releaseMemory()
            val asString = CrashReport("Reden generated crash report.", e).asString()
            httpClient.newCall(Request.Builder().apply {
                url("$redenApiBaseUrl/mc/exception")
                @Serializable
                class Req(
                    val key: String,
                    val crash: String,
                )
                json(Req(key, asString))
                ua()
            }.build()).execute().use {
                @Serializable
                class Res(
                    val status: String,
                    val shutdown: Boolean,
                )

                val res = jsonIgnoreUnknown.decodeFromString(Res.serializer(), it.body!!.string())
            }
            return
        } catch (_: Exception) {
        }
    }
}

class UpdateInfo(
    val version: String,
    val url: String,
    val changelog: String,
    val type: String,
)

fun checkUpdateFromModrinth(): UpdateInfo? {
    @Serializable
    data class ModrinthFile(
        val url: String,
        val filename: String,
        val size: Long
    )

    @Serializable
    data class ModrinthVersion(
        val id: String,
        val name: String,
        val version_number: String,
        val changelog: String,
        val game_versions: List<String>,
        val files: List<ModrinthFile>
    )

    val modrinthVersion = FabricLoader.getInstance().getModContainer(Reden.MOD_ID)
        .get().metadata.getCustomValue("modmenu").asObject.get("modrinth").asString
    val res = httpClient.newCall(Request.Builder().apply {
        url("https://api.modrinth.com/v2/project/$modrinthVersion/version")
        ua()
    }.build()).execute().use {
        it.body!!.string()
    }
    val curVersion = SharedConstants.getGameVersion().name
    val versions =
        jsonIgnoreUnknown.decodeFromString<List<ModrinthVersion>>(res).filter { curVersion in it.game_versions }
    val latest = versions.maxByOrNull { Version.parse(it.version_number) }
    return if (latest != null && Version.parse(latest.version_number) > Reden.MOD_VERSION)
        UpdateInfo(latest.version_number, latest.files.first().url, latest.changelog, "modrinth")
    else null
}

fun checkUpdateFromRedenApi(): UpdateInfo? {
    return null // todo
}

fun checkAnnouncements() {
    httpClient.newCall(Request.Builder().apply {
        ua()
    }.build())
}

@Serializable
private class ModData(
    val name: String,
    val version: String,
    val modid: String,
    val authors: List<String>
)

@Serializable
private class OnlineReq(
    val name: String,
    val early_access: Boolean,
    var online_mode: Boolean,
    val os: String,
    val cpus: Int,
    val mc_version: String,
    val reden_version: String,
    val mods: List<ModData>,
    val servers: List<Map<String, String>>
)

@Serializable
class OnlineRes(
    val shutdown: Boolean = false,
    val key: String? = null,
    val ip: String = "",
    val id: String? = null,
    val status: String = "",
    val username: String? = null,
    val desc: String = "",
)

fun updateOnlineInfo(client: MinecraftClient): Boolean {
    if (heartbeatThread == null || !heartbeatThread!!.isAlive) {
        initHeartBeat()
    }
    if (client.userApiService == UserApiService.OFFLINE) return false
    try {
        client.sessionService.joinServer(
            client.session.uuidOrNull,
            client.session.accessToken,
            "3cb49a79c3af1f1dba6c56eddd760ac7d50c518a"
        )

        val serverList = ServerList(client).also(ServerList::loadFile)
        val req = OnlineReq(
            name = client.session.username,
            early_access = false,
            online_mode = client.userApiService != UserApiService.OFFLINE,
            os = System.getProperty("os.name") + " " + System.getProperty("os.version"),
            cpus = Runtime.getRuntime().availableProcessors(),
            mc_version = MinecraftVersion.create().name,
            reden_version = Reden.MOD_VERSION.friendlyString,
            mods = FabricLoader.getInstance().allMods.map {
                ModData(
                    it.metadata.name,
                    it.metadata.version.toString(),
                    it.metadata.id,
                    listOf()
                )
            },
            servers = (0 until serverList.size()).map { serverList[it] }.map {
                mapOf(
                    "name" to it.name,
                    "ip" to it.address,
                )
            }
        )
        val res = httpClient.newCall(Request.Builder().apply {
            url("$redenApiBaseUrl/mc/online")
            json(req)
            ua()
        }.build()).execute().body!!.string()
        key = requireNotNull(jsonIgnoreUnknown.decodeFromString<OnlineRes>(res).apply {
            if (shutdown) return false
        }.key) { "Reden ApiKey is null" }
        return true
    } catch (e: Exception) {
        LOGGER.error("Failed to login", e)
        return false
    }
}

fun redenSetup(client: MinecraftClient) {
    Thread {
        try {
            val serverList = ServerList(client)
            serverList.loadFile()
            val req = OnlineReq(
                name = if (data_IDENTIFICATION.booleanValue) client.session.username else "Anonymous",
                early_access = false,
                online_mode = client.userApiService != UserApiService.OFFLINE,
                os = System.getProperty("os.name") + " " + System.getProperty("os.version"),
                cpus = Runtime.getRuntime().availableProcessors(),
                mc_version = MinecraftVersion.create().name,
                reden_version = Reden.MOD_VERSION.friendlyString,
                mods = if (data_IDENTIFICATION.booleanValue) FabricLoader.getInstance().allMods.map {
                    ModData(
                        it.metadata.name,
                        it.metadata.version.toString(),
                        it.metadata.id,
                        listOf()
                    )
                }
                else listOf(),
                servers = if (data_IDENTIFICATION.booleanValue) (0 until serverList.size()).map { serverList[it] }.map {
                    mapOf(
                        "name" to it.name,
                        "ip" to it.address,
                    )
                }
                else listOf()
            )
            try {
                client.sessionService.joinServer(
                    client.session.uuidOrNull,
                    client.session.accessToken,
                    "3cb49a79c3af1f1dba6c56eddd760ac7d50c518a"
                )
            } catch (e: Exception) {
                LOGGER.error("", e)
                req.online_mode = false
            }

            val res =
                jsonIgnoreUnknown.decodeFromString(OnlineRes.serializer(), httpClient.newCall(Request.Builder().apply {
                url("$redenApiBaseUrl/mc/online")
                json(req)
                ua()
            }.build()).execute().body!!.string())
            if (res.shutdown) {
                throw Error("Client closing due to copyright reasons, please go to https://www.redenmc.com/policy/copyright gor more information")
            }
            key = requireNotNull(res.key) { "Reden ApiKey is null" }
            initHeartBeat()
            LOGGER.info("RedenMC: ${res.desc}")
            LOGGER.info("key=${res.key}, ip=${res.ip}, id=${res.id}, status=${res.status}, username=${res.username}")
        } catch (e: Exception) {
            LOGGER.error("", e)
        }
        updateOnlineInfo(client)
    }.start()
    Runtime.getRuntime().addShutdownHook(Thread {
        try {
            if (featureUsageData.isNotEmpty()) doHeartHeat()
        } catch (e: Exception) {
            LOGGER.error("", e)
        }
        try {
            @Serializable
            class Req(
                val key: String
            )
            httpClient.newCall(Request.Builder().apply {
                url("$redenApiBaseUrl/mc/offline")
                json(Req(key))
                ua()
            }.build()).execute().use {
            }
        } catch (e: Exception) {
            LOGGER.error("", e)
        }
    })
    if (HiddenOption.iCHECK_UPDATES.booleanValue) {
        Thread {
            val updateInfo = try {
                checkUpdateFromRedenApi() ?: checkUpdateFromModrinth()
            } catch (e: Exception) {
                LOGGER.error("", e)
                null
            }
            if (updateInfo != null) {
                ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
                    client.player?.sendMessage(
                        Text.literal("RedenMC: New version ${updateInfo.version} is available, download at ${updateInfo.url}")
                            .styled {
                                it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, updateInfo.url))
                            })
                }
            }
        }.start()
    }
}
