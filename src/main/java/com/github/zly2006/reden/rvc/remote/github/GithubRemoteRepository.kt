package com.github.zly2006.reden.rvc.remote.github

import com.github.zly2006.reden.malilib.GITHUB_TOKEN
import com.github.zly2006.reden.rvc.remote.IRemoteRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GithubRemoteRepository(
    val token: String,
    val name: String,
    val owner: String,
): IRemoteRepository {
    @Suppress("unused", "PropertyName")
    @Serializable
    class CreateSettings(
        val name: String,
        val private: Boolean = false,
        val description: String = "",
        val has_issues: Boolean = true,
        val has_projects: Boolean = false,
        val has_wiki: Boolean = false,
        val auto_init: Boolean = false,
        val license_template: String,
    )
    val client = OkHttpClient()
    fun create(settings: CreateSettings) {
        val response = client.newCall(
            Request.Builder().url("https://api.github.com/user/repos")
                .post(
                    Json.encodeToString(CreateSettings.serializer(), settings)
                        .toRequestBody("application/json".toMediaType())
                )
                .header("Accept", "application/json")
                .githubToken()
                .build()
        ).execute()
        if (!response.isSuccessful) throw Exception("Failed to create repo: ${response.code} ${response.message}")
    }

    override fun deleteRepo() {
        client.newCall(Request.Builder().url("https://api.github.com/repos/$owner/$name").githubToken().delete().build()).execute()
    }

    override val gitUrl = "https://github.com//$owner/$name.git"
}

private fun Request.Builder.githubToken() = header("Authorization", GITHUB_TOKEN.stringValue)
