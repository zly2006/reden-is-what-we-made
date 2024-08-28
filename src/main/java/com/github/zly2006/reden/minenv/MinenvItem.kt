package com.github.zly2006.reden.minenv

import kotlinx.serialization.Serializable

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

    )

