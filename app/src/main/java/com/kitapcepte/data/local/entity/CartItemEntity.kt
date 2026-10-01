package com.kitapcepte.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["bookId"], unique = true)]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val price: Double,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)
