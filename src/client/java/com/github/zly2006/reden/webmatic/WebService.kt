@file:Suppress("unused", "PropertyName")

package com.github.zly2006.reden.webmatic

import com.github.zly2006.reden.Reden.*
import com.github.zly2006.reden.utils.isClient
import com.github.zly2006.reden.utils.multiver.Text
import com.github.zly2006.reden.utils.multiver.clickOpenUrl
import com.github.zly2006.reden.utils.multiver.sendSystemMessage
import com.mojang.authlib.exceptions.InvalidCredentialsException
import com.mojang.authlib.minecraft.UserApiService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.DetectedVersion
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ServerList
import net.minecraft.server.MinecraftServer
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.userAgent
import okio.use
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

const val redenApiBaseUrl = "https://api.redenmc.com/api"
var key = ""
val gameVer = DetectedVersion.tryDetectVersion()!!
//? if < 1.21.6 {
val gameVerString = gameVer.name!!
//?} else {
/*val gameVerString = gameVer.name()!!
*///?}


val httpClient = OkHttpClient.Builder().apply {
    readTimeout(60.seconds.toJavaDuration())
    cache(
        Cache(
            directory = File(".cache", "reden"),
            maxSize = 100L * 1024L * 1024L // 100 MiB
        )
    )
    Logger.getLogger(OkHttpClient::class.java.name).level = Level.FINE
}.build()

inline fun <reified T> Request.Builder.json(data: T) = apply {
    header("Content-Type", "application/json")
    post(Json.encodeToString(data).toRequestBody("application/json".toMediaTypeOrNull()))
}

fun Request.Builder.ua() = apply {
    header("Authorization", "ApiKey $key")
    header("User-Agent", "RedenMC/${MOD_VERSION} Minecraft/$gameVerString (Fabric) $userAgent")
}

@Serializable
class FeatureUsageData(
    val source: String,
    val name: String,
    val time: Long,
)

fun doHeartHeat() {
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
            Minecraft.getInstance().connection?.onlinePlayers?.map { Player(
                it.profile.name,
                it.profile.id.toString(),
                it.latency,
                it.gameMode.name,
            ) }
        } else emptyList()
        val req = Req(
            key,
            featureUsageData,
            usedTimes,
            samplePlayers()
        )
        json(req)
        ua()
    }.build()).execute().use {
        @Serializable
        class Res(
            val status: String,
            val shutdown: Boolean,
        )

        if (it.code in 200..299 || it.code in 400..499) {
            val res = jsonIgnoreUnknown.decodeFromString(Res.serializer(), it.body!!.string())
            if (res.shutdown) {
                throw Error(res.status)
            }
            if (res.status.startsWith("set-key=")) {
                key = res.status.substring(8)
                updateOnlineInfo(Minecraft.getInstance())
            }
            if (it.code == 200) {
                featureUsageData.clear()
            }
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
private var activeUseTimes = 0

fun onFunctionUsed(name: String, active: Boolean = false) {
    featureUsageData.add(FeatureUsageData(if (isClient) Minecraft.getInstance().user.name else "Server", name, System.currentTimeMillis()))
    if (heartbeatThread == null || !heartbeatThread!!.isAlive) {
        initHeartBeat()
    }
    usedTimes++
    activeUseTimes++
    if (isClient) {
    }
}

val jsonIgnoreUnknown = Json { ignoreUnknownKeys = true }

fun reportServerStart(server: MinecraftServer) {

}

fun reportException(e: Exception) {
//    if (isDevVersion) {
//        try {
//            CrashMemoryReserve.releaseMemory()
//            val asString = CrashReport("Reden generated crash report.", e).asString(ReportType.MINECRAFT_CRASH_REPORT)
//            httpClient.newCall(Request.Builder().apply {
//                url("$redenApiBaseUrl/mc/exception")
//                @Serializable
//                class Req(
//                    val key: String,
//                    val crash: String,
//                )
//                json(Req(key, asString))
//                ua()
//            }.build()).execute().use {
//                @Serializable
//                class Res(
//                    val status: String,
//                    val shutdown: Boolean,
//                )
//
//                val res = jsonIgnoreUnknown.decodeFromString(Res.serializer(), it.body!!.string())
//            }
//            return
//        } catch (_: Exception) {
//        }
//    }
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

    val modrinthVersion = FabricLoader.getInstance().getModContainer(MOD_ID)
        .get().metadata.getCustomValue("modmenu").asObject.get("modrinth").asString
    val res = httpClient.newCall(Request.Builder().apply {
        url("https://api.modrinth.com/v2/project/$modrinthVersion/version")
        ua()
    }.build()).execute().use {
        it.body!!.string()
    }
    val versions =
        jsonIgnoreUnknown.decodeFromString<List<ModrinthVersion>>(res).filter { gameVerString in it.game_versions }
    val latest = versions.maxByOrNull { Version.parse(it.version_number) }
    return if (latest != null && Version.parse(latest.version_number) > Version.parse(MOD_VERSION))
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

fun updateOnlineInfo(client: Minecraft): Boolean {
    if (heartbeatThread == null || !heartbeatThread!!.isAlive) {
        initHeartBeat()
    }
    try {
        client.minecraftSessionService.joinServer(
            client.user.profileId,
            client.user.accessToken,
            "3cb49a79c3af1f1dba6c56eddd760ac7d50c518a"
        )

        val serverList = ServerList(client).also(ServerList::load)
        val req = OnlineReq(
            name = client.user.name,
            early_access = false,
            online_mode = client.userApiService != UserApiService.OFFLINE,
            os = System.getProperty("os.name") + " " + System.getProperty("os.version"),
            cpus = Runtime.getRuntime().availableProcessors(),
            mc_version = gameVerString,
            reden_version = MOD_VERSION,
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
                    "ip" to it.ip,
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
    } catch (_: InvalidCredentialsException) {
        LOGGER.error("Cannot log you in, are you using an online minecraft account?")
        return false
    } catch (e: Exception) {
        LOGGER.error("Failed to login", e)
        return false
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun redenSetup(client: Minecraft) {
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val serverList = ServerList(client)
            serverList.load()
            val req = OnlineReq(
                name = client.user.name,
                early_access = false,
                online_mode = client.userApiService != UserApiService.OFFLINE,
                os = System.getProperty("os.name") + " " + System.getProperty("os.version"),
                cpus = Runtime.getRuntime().availableProcessors(),
                mc_version = gameVerString,
                reden_version = MOD_VERSION,
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
                        "ip" to it.ip,
                    )
                }
            )
            try {
                client.minecraftSessionService.joinServer(
                    client.user.profileId,
                    client.user.accessToken,
                    "3cb49a79c3af1f1dba6c56eddd760ac7d50c518a"
                )
            } catch (_: InvalidCredentialsException) {
                req.online_mode = false
                LOGGER.warn("Failed to login to minecraft, using offline mode.")
            } catch (e: Exception) {
                LOGGER.error("", e)
                req.online_mode = false
            }

            val res = jsonIgnoreUnknown.decodeFromString<OnlineRes>(httpClient.newCall(Request.Builder().apply {
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
    GlobalScope.launch(Dispatchers.IO) {
        val updateInfo = try {
            checkUpdateFromRedenApi() ?: checkUpdateFromModrinth()
        } catch (e: Exception) {
            LOGGER.error("", e)
            null
        }
        if (updateInfo != null) {
            ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
                client.player?.sendSystemMessage(
                    Text.literal("RedenMC: New version ${updateInfo.version} is available, download at ${updateInfo.url}")
                        .clickOpenUrl(updateInfo.url))
            }
        }
    }.start()
}
