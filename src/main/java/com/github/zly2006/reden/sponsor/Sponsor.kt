package com.github.zly2006.reden.sponsor

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers

@Serializable
class Sponsor(
    val name: String,
    val avatar: String,
    val description: String,
    val amount: Double,
)

var sponsors = listOf<Sponsor>(); private set

fun updateSponsors() {
    try {
        val httpClient = HttpClient.newHttpClient()
        val str = httpClient.send(HttpRequest.newBuilder(URI("https://www.starlight.cool/reden/api/sponsor")).build(), BodyHandlers.ofString()).body()
        sponsors = Json.decodeFromString(ListSerializer(Sponsor.serializer()), str)
    }
    catch (_: Exception) {

    }
}
