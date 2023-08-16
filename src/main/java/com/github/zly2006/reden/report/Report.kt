package com.github.zly2006.reden.report

import com.google.gson.Gson
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.MinecraftVersion
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.ServerList
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.util.*

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
    val metadata = ClientMetadataReq(
        online_mode = mc.session.accessToken != "FabricMC",
        uuid = mc.session.uuidOrNull,
        name = mc.session.username,
        mcversion = mc.gameVersion + " " + MinecraftVersion.create().name,
        servers = (0 until servers.size()).map { servers[it] }.map {
            ClientMetadataReq.Server(
                name = it.name,
                ip = it.address,
                motd = ""
            )
        }
    )
    try {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
            httpClient.send(
                HttpRequest.newBuilder(URI("https://slv4.starlight.cool:4321/mcdata/client"))
                    .POST(BodyPublishers.ofString(Gson().toJson(metadata)))
                    .header("Content-Type", "application/json")
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
        }
    } catch (_: Exception) {
    }
}
