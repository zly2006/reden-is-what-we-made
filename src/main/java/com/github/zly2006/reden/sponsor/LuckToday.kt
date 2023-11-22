package com.github.zly2006.reden.sponsor

import com.github.zly2006.reden.report.httpClient
import com.github.zly2006.reden.report.ua
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Request

class LuckToday {
    @Serializable
    class LuckTodayResponse(
        val data: Int,
        val date: String,
        val id: String
    )
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        val luckValue by lazy {
            httpClient.newCall(Request.Builder().apply {
                ua()
                url("https://redenmc.com/api/mc/luck-today")
            }.build()).execute().use {
                json.decodeFromString<LuckTodayResponse>(it.body!!.string())
            }
        }
    }
}