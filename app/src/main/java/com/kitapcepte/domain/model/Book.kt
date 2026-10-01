package com.kitapcepte.domain.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val price: Double,
    val originalPrice: Double? = null,
    val isFavorite: Boolean = false,
    val isTopItem: Boolean = false,
    val editionCount: Int = 0,
    val firstPublishYear: Int? = null,
    val category: BookCategory = BookCategory.DEFAULT,
    val description: String? = null
)
