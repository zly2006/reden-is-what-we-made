package com.github.zly2006.reden.minenv

import io.wispforest.owo.ui.container.FlowLayout
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class MevItem(
    val post_name: String,
    val uuid: String,
    val description: String,
    val tags: List<String>,
    val versions: List<String>,
    val User: String,
    val yt_link: String,
    val downloads: Long,
    val images: List<String>,
    val published_at: Instant,
    val thumbnail_url: String?,
    @Transient
    var display: FlowLayout? = null
)

