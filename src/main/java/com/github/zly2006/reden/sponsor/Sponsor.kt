package com.github.zly2006.reden.sponsor

import com.github.zly2006.reden.Reden
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

@Serializable
class Sponsor(
    val name: String,
    val avatar: String? = null,
    val detail: String? = null,
    val message: String = "",
    val amount: Double,
)

var sponsors = listOf<Sponsor>(); private set

fun updateSponsors() {
    try {
        OkHttpClient().newCall(Request.Builder().apply {
            url("https://www.redenmc.com/api/sponsors")
        }.build()).execute().use { res ->
            if (res.code == 200)
                sponsors = Json.decodeFromString(ListSerializer(Sponsor.serializer()), res.body!!.string())
                    .sortedBy { -it.amount }
            else
                Reden.LOGGER.info("Failed to update sponsors. Status Code = ${res.code}")
        }
    }
    catch (e: Exception) {
        Reden.LOGGER.info("Failed to update sponsors.", e)
    }
}
