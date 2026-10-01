package com.kitapcepte.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_cards",
    indices = [Index(value = ["userId"])]
)
data class SavedCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val cardHolder: String,
    val maskedNumber: String,
    val cardBrand: String,
    val expiryMonth: String,
    val expiryYear: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
