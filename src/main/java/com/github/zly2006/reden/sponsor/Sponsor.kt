package com.github.zly2006.reden.sponsor

import com.github.zly2006.reden.Reden
import com.github.zly2006.reden.utils.isClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.minecraft.client.MinecraftClient
import okhttp3.*
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger


@Serializable
class Sponsor(
    val name: String,
    val avatar: String? = null,
    val detail: String? = null,
    val message: String = "",
    val amount: Double,
)

var sponsors: List<Sponsor>? = null
    get() = if (time + 1000 * 60 < System.currentTimeMillis()) null else field
    private set
private var time = 0L
val x by lazy {
    8
}

fun updateSponsors() {
    Logger.getLogger(OkHttpClient::class.java.getName()).setLevel(Level.FINE)
    OkHttpClient.Builder().build().newCall(Request.Builder().apply {
        url("https://www.redenmc.com/api/sponsors")
    }.build()).enqueue(object : Callback {
        fun updateClient() {
            if (isClient) {
                val mc = MinecraftClient.getInstance()
                val screen = mc.currentScreen
                mc.execute {
                    if (screen is SponsorScreen) {
                        mc.setScreen(SponsorScreen(screen.parent, false))
                    }
                }
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            Reden.LOGGER.info("Failed to update sponsors.", e)
            updateClient()
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.code == 200) {
                sponsors = Json.decodeFromString(ListSerializer(Sponsor.serializer()), response.body!!.string())
                    .sortedBy { -it.amount }
                Reden.LOGGER.info("Updated sponsors.")
                time = System.currentTimeMillis()
            } else {
                Reden.LOGGER.info("Failed to update sponsors. Status Code = ${response.code}")
                response.close()
            }
            updateClient()
        }
    })
}
