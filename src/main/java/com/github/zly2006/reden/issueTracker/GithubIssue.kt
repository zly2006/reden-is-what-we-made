package com.github.zly2006.reden.issueTracker

import com.github.zly2006.reden.malilib.GITHUB_TOKEN
import com.github.zly2006.reden.report.httpClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Request

private val json = Json {
    ignoreUnknownKeys = true
}

@OptIn(ExperimentalSerializationApi::class)
class GithubIssue(
    val user: String,
    val repo: String,
    val id: Int
) {
    @Serializable
    data class IssueBodyRes(
        val html_url: String,
        val user: User,
        val labels: List<Label>,
        val timeline_url: String,
        val state: State
    ) {
        @Serializable
        enum class State {
            open,
            closed
        }
    }
    @Serializable
    data class User(
        val login: String,
        val avatar_url: String,
        val html_url: String,
        val type: Type
    ) {
        @Serializable
        enum class Type {
            User,
            Organization
        }
    }
    @Serializable
    data class Label(
        val name: String,
        val description: String,
        val color: String
    )
    @Serializable
    data class TimelineItem(
        val id: Long,
        val url: String,
        val actor: User,
        val event: String,
        val created_at: String,
        /**
         * [event]` == "labeled"`
         */
        val label: TimeLineLabeld? = null,
        /**
         * [event]` == "commented"`
         */
        val body: String? = null,
        /**
         * [event]` == "referenced"`
         */
        val commit_id: String? = null,
        /**
         * [event]` == "referenced"`
         */
        val commit_url: String? = null,
    ) {
        companion object {
            const val labeled = "labeled"
            const val commented = "commented"
            const val referenced = "referenced"
            const val closed = "closed"
        }
        @Serializable
        data class TimeLineLabeld(
            val name: String,
            val color: String
        )
    }
    val url = "https://api.github.com/repos/$user/$repo/issues/$id"
    val info = httpClient.newCall(Request.Builder().apply {
        url(url)
        githubApiKey()
    }.build()).execute().use {
        if (it.code == 200) {
            json.decodeFromStream<IssueBodyRes>(it.body!!.byteStream())
        }
        else {
            throw RuntimeException("Failed to get Github issue: Code=${it.code}, Response=${it.body?.string()}")
        }
    }
    val timeline = httpClient.newCall(Request.Builder().apply {
        url(info.timeline_url)
        githubApiKey()
    }.build()).execute().use {
        val s = it.body!!.string()
        json.decodeFromString<List<TimelineItem>>(s)
    }
    fun update() {
    }
}

private fun Request.Builder.githubApiKey() {
    if (GITHUB_TOKEN.stringValue.isNotEmpty())
        header("Authorization", GITHUB_TOKEN.stringValue)
}
