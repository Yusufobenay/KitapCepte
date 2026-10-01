package com.kitapcepte.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_books")
data class FavoriteBookEntity(
    @PrimaryKey
    val bookId: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val price: Double,
    val originalPrice: Double? = null,
    val categorySlug: String,
    val isTopItem: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
