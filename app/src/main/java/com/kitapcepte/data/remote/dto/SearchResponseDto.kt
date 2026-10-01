package com.kitapcepte.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResponseDto(
    val numFound: Int = 0,
    val start: Int = 0,
    val docs: List<BookDocDto> = emptyList()
)

@Serializable
data class BookDocDto(
    val key: String? = null,
    val title: String? = null,
    @SerialName("author_name")
    val authorName: List<String>? = null,
    @SerialName("cover_i")
    val coverId: Long? = null,
    @SerialName("first_publish_year")
    val firstPublishYear: Int? = null,
    @SerialName("edition_count")
    val editionCount: Int? = null,
    val language: List<String>? = null
)
