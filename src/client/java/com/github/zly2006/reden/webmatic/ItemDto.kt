package com.github.zly2006.reden.webmatic

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

enum class PostType {
    LitematicaGen,
    LitematicaShare,
}

enum class DownloadType {
    Litematica, MaterialList, Edit,
}

enum class PostStatus {
    Pending, Approved, Rejected, Deleted, TakenDown;

    companion object {
        fun get(value: String?) = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: Pending
    }
}

@Suppress("EnumEntryName", "unused")
enum class PostSource {
    self,
    minemev,
}

@Serializable
data class FileItem(
    val name: String,
    val url: String,
    val size: Long,
    val description: String? = null,
)

@Serializable
data class UserOrientedData(
    val owner: Boolean = false,
    val vote: Boolean? = null,
    val favorite: Boolean = false,
    val bookmark: Boolean = false,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BasicModel(
    val id: Int = -1,
    val avatarUrl: String? = null,
    val username: String,
    val isStaff: Boolean = false,
)

@Serializable
data class ItemDto(
    val type: PostType,
    val name: String,
    val key: String,
    val hasX: Boolean = false,
    val hasY: Boolean = false,
    val hasZ: Boolean = false,
//    val conditions: Conditions = Conditions(),
    val link: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val updatedAt: Long,
    val author: BasicModel?,
    val imageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val attachments: List<FileItem> = emptyList(),
    var downloads: Long = -1,
    val images: List<String> = emptyList(),
//    val categoryTag: TagService.BriefDto? = null,
//    val featureTags: List<TagService.BriefDto> = emptyList(),
    val versions: List<String> = emptyList(),
    val original: Boolean = false,
    val status: PostStatus = PostStatus.Pending,
    var ud: UserOrientedData? = null,
    var upVotes: Long,
    var downVotes: Long,
)
